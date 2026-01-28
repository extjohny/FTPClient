package ru.abdulkhalikov.ftpclient.ai

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp

class ImageClassifier(context: Context) {

    private var interpreter: Interpreter? = null
    private val labels: List<String>

    companion object {
        private const val TAG = "ImageClassifier"
        private const val MODEL_FILE = "mobilenet.tflite"
        private const val LABEL_FILE = "labels.txt"
        private const val IMAGE_SIZE = 224
        private const val IMAGE_MEAN = 127.5f
        private const val IMAGE_STD = 127.5f
        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 3
    }

    init {
        try {
            // Загружаем модель
            val modelBuffer = loadModelFile(context.assets, MODEL_FILE)

            // Настройка интерпретатора
            val options = Interpreter.Options()
            val compatList = CompatibilityList()

            if (compatList.isDelegateSupportedOnThisDevice) {
                options.addDelegate(GpuDelegate(compatList.bestOptionsForThisDevice))
            }

            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "MobileNet модель загружена успешно")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки MobileNet: ${e.message}")
        }

        // Загружаем метки
        labels = try {
            loadLabelList(context.assets, LABEL_FILE)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки labels.txt: ${e.message}")
            emptyList()
        }
    }

    /**
     * Классифицирует изображение по URI
     */
    fun classifyImage(uri: Uri, context: Context): ClassificationResult {
        return try {
            val bitmap = loadBitmapFromUri(uri, context)
                ?: return ClassificationResult.error("Не удалось загрузить изображение")

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, false)
            val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)

            val output = Array(1) { FloatArray(labels.size) }
            interpreter?.run(byteBuffer, output)

            val results = processOutput(output[0])
            ClassificationResult.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка классификации: ${e.message}")
            ClassificationResult.error("Ошибка анализа: ${e.message}")
        }
    }

    /**
     * Преобразует Bitmap в ByteBuffer для модели
     */
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(
            FLOAT_TYPE_SIZE * IMAGE_SIZE * IMAGE_SIZE * PIXEL_SIZE
        )
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(IMAGE_SIZE * IMAGE_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (y in 0 until IMAGE_SIZE) {
            for (x in 0 until IMAGE_SIZE) {
                val pixelValue = intValues[pixel++]

                // Нормализация значений пикселей
                byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / IMAGE_STD - IMAGE_MEAN / IMAGE_STD)
                byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / IMAGE_STD - IMAGE_MEAN / IMAGE_STD)
                byteBuffer.putFloat((pixelValue and 0xFF) / IMAGE_STD - IMAGE_MEAN / IMAGE_STD)
            }
        }

        return byteBuffer
    }

    /**
     * Обработка выходных данных модели
     */
    private fun processOutput(output: FloatArray): List<Prediction> {
        // Применяем softmax для получения вероятностей
        val expOutput = output.map { exp(it) }
        val sumExp = expOutput.sum()
        val probabilities = expOutput.map { it / sumExp }

        // Получаем топ-3 предсказания
        return probabilities
            .mapIndexed { index, prob -> Prediction(labels[index], prob) }
            .sortedByDescending { it.confidence }
            .take(3)
    }

    /**
     * Загрузка Bitmap из URI
     */
    private fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Загрузка модели из assets
     */
    @Throws(Exception::class)
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Загрузка списка меток
     */
    @Throws(Exception::class)
    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readLines()
            }
        }
    }

    /**
     * Закрытие интерпретатора
     */
    fun close() {
        interpreter?.close()
    }

    /**
     * Проверка доступности модели
     */
    fun isAvailable(): Boolean {
        return interpreter != null
    }

    data class Prediction(
        val label: String,
        val confidence: Float
    )

    sealed class ClassificationResult {
        data class Success(val predictions: List<Prediction>) : ClassificationResult()
        data class Error(val message: String) : ClassificationResult()

        companion object {
            fun success(predictions: List<Prediction>) = Success(predictions)
            fun error(message: String) = Error(message)
        }
    }
}