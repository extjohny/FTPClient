package ru.abdulkhalikov.ftpclient.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import java.io.BufferedReader
import java.io.InputStreamReader

class TextClassifier(context: Context) {

    private var classifier: NLClassifier? = null

    companion object {
        private const val TAG = "TextClassifier"
        private const val MODEL_FILE = "text_model.tflite"

        // Категории для текста
        private val CATEGORY_EMOJIS = mapOf(
            "TECHNICAL" to "⚙️",
            "BUSINESS" to "💼",
            "LEGAL" to "⚖️",
            "ACADEMIC" to "🔬",
            "NEWS" to "📰",
            "PERSONAL" to "📝",
            "CODE" to "💻",
            "DATA" to "📊"
        )
    }

    init {
        try {
            classifier = NLClassifier.createFromFile(context, MODEL_FILE)
            Log.d(TAG, "BERT модель загружена успешно")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки BERT модели: ${e.message}")
        }
    }

    /**
     * Классифицирует текстовый файл
     */
    fun classifyText(uri: Uri, context: Context): TextClassificationResult {
        return try {
            val text = readTextFromUri(uri, context, 5000)
            if (text.isBlank()) {
                return TextClassificationResult.error("Пустой текстовый файл")
            }

            val results = classifier?.classify(text) ?: emptyList()
            if (results.isEmpty()) {
                return TextClassificationResult.error("Не удалось классифицировать текст")
            }

            // Берем топ-1 результат
            val topResult = results.maxByOrNull { it.score }
                ?: return TextClassificationResult.error("Нет результатов")

            val mappedCategory = mapCategory(topResult.category)

            TextClassificationResult.success(
                category = mappedCategory.name,
                confidence = topResult.score,
                emoji = mappedCategory.emoji,
                details = "Текст: ${text.take(100)}..."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка классификации текста: ${e.message}")
            TextClassificationResult.error("Ошибка анализа текста")
        }
    }

    /**
     * Чтение текста из файла
     */
    private fun readTextFromUri(uri: Uri, context: Context, maxChars: Int): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
                val text = StringBuilder()
                var line: String?
                var totalChars = 0

                while (reader.readLine().also { line = it } != null && totalChars < maxChars) {
                    text.append(line).append("\n")
                    totalChars += line?.length ?: 0
                }

                text.toString()
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Маппинг категорий на русский язык и эмодзи
     */
    private fun mapCategory(category: String): CategoryInfo {
        val upperCategory = category.uppercase()

        return when {
            upperCategory.contains("TECHNICAL") || upperCategory.contains("TECH") ->
                CategoryInfo("Технический текст", "⚙️")

            upperCategory.contains("BUSINESS") || upperCategory.contains("FINANCE") ->
                CategoryInfo("Бизнес-документ", "💼")

            upperCategory.contains("LEGAL") || upperCategory.contains("LAW") ->
                CategoryInfo("Юридический документ", "⚖️")

            upperCategory.contains("ACADEMIC") || upperCategory.contains("SCIENCE") ->
                CategoryInfo("Научный текст", "🔬")

            upperCategory.contains("NEWS") || upperCategory.contains("ARTICLE") ->
                CategoryInfo("Новость/Статья", "📰")

            upperCategory.contains("PERSONAL") || upperCategory.contains("DIARY") ->
                CategoryInfo("Личная запись", "📝")

            upperCategory.contains("CODE") || upperCategory.contains("PROGRAM") ->
                CategoryInfo("Программный код", "💻")

            else -> CategoryInfo("Текст", "📄")
        }
    }

    /**
     * Проверка доступности модели
     */
    fun isAvailable(): Boolean {
        return classifier != null
    }

    /**
     * Закрытие классификатора
     */
    fun close() {
        classifier?.close()
    }

    data class CategoryInfo(
        val name: String,
        val emoji: String
    )

    data class TextClassificationResult(
        val success: Boolean,
        val category: String? = null,
        val confidence: Float? = null,
        val emoji: String? = null,
        val details: String? = null,
        val error: String? = null
    ) {
        companion object {
            fun success(
                category: String,
                confidence: Float,
                emoji: String,
                details: String
            ) = TextClassificationResult(
                success = true,
                category = category,
                confidence = confidence,
                emoji = emoji,
                details = details
            )

            fun error(message: String) = TextClassificationResult(
                success = false,
                error = message
            )
        }
    }
}