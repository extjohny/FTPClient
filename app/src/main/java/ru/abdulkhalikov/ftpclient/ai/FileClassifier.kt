package ru.abdulkhalikov.ftpclient.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileClassifier(context: Context) {

    private val imageClassifier = ImageClassifier(context)
    private val textClassifier = TextClassifier(context)

    companion object {
        private const val TAG = "FileClassifier"

        // Разрешенные расширения для анализа
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        private val TEXT_EXTENSIONS = setOf("txt", "md", "log", "ini", "cfg", "xml", "json")
        private val CODE_EXTENSIONS = setOf(
            "java", "kt", "cpp", "c", "h", "py", "js", "ts",
            "html", "css", "php", "rb", "go", "rs", "swift"
        )
        private val DOCUMENT_EXTENSIONS = setOf("pdf", "doc", "docx", "odt", "rtf")
        private val DATA_EXTENSIONS = setOf("xls", "xlsx", "csv", "tsv", "ods")
        private val ARCHIVE_EXTENSIONS = setOf("zip", "rar", "7z", "tar", "gz")
        private val MEDIA_EXTENSIONS = setOf("mp4", "avi", "mov", "mkv", "mp3", "wav", "flac")
    }

    /**
     * Основной метод классификации файла
     */
    suspend fun classifyFile(uri: Uri, fileName: String, context: Context): ClassificationResult {
        return withContext(Dispatchers.IO) {
            try {
                val extension = getFileExtension(fileName).lowercase()

                when {
                    // Изображения - через MobileNet
                    IMAGE_EXTENSIONS.contains(extension) && imageClassifier.isAvailable() -> {
                        classifyImage(uri, context, fileName)
                    }

                    // Текстовые файлы - через BERT
                    TEXT_EXTENSIONS.contains(extension) && textClassifier.isAvailable() -> {
                        classifyText(uri, context, fileName)
                    }

                    // Код
                    CODE_EXTENSIONS.contains(extension) -> {
                        ClassificationResult(
                            category = "Код: ${getLanguageName(extension)}",
                            confidence = 0.95f,
                            emoji = "💻",
                            details = "Исходный код ${getLanguageName(extension)}"
                        )
                    }

                    // Документы
                    DOCUMENT_EXTENSIONS.contains(extension) -> {
                        ClassificationResult(
                            category = "Документ",
                            confidence = 0.9f,
                            emoji = "📄",
                            details = "Документ формата .$extension"
                        )
                    }

                    // Таблицы
                    DATA_EXTENSIONS.contains(extension) -> {
                        ClassificationResult(
                            category = "Таблица данных",
                            confidence = 0.9f,
                            emoji = "📊",
                            details = "Файл с табличными данными"
                        )
                    }

                    // Архивы
                    ARCHIVE_EXTENSIONS.contains(extension) -> {
                        ClassificationResult(
                            category = "Архив",
                            confidence = 0.9f,
                            emoji = "🗜️",
                            details = "Сжатый архив .$extension"
                        )
                    }

                    // Медиа
                    MEDIA_EXTENSIONS.contains(extension) -> {
                        ClassificationResult(
                            category = getMediaType(extension),
                            confidence = 0.9f,
                            emoji = getMediaEmoji(extension),
                            details = "Медиафайл .$extension"
                        )
                    }

                    // По умолчанию
                    else -> {
                        ClassificationResult(
                            category = "Файл .$extension",
                            confidence = 0.7f,
                            emoji = "📎",
                            details = "Тип: .$extension"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка классификации файла: ${e.message}")
                ClassificationResult(
                    category = "Ошибка анализа",
                    confidence = 0.0f,
                    emoji = "❌",
                    details = "Не удалось проанализировать файл"
                )
            }
        }
    }

    /**
     * Классификация изображений через MobileNet
     */
    private suspend fun classifyImage(uri: Uri, context: Context, fileName: String): ClassificationResult {
        return withContext(Dispatchers.IO) {
            val result = imageClassifier.classifyImage(uri, context)

            when (result) {
                is ImageClassifier.ClassificationResult.Success -> {
                    val topPrediction = result.predictions.firstOrNull()

                    if (topPrediction != null && topPrediction.confidence > 0.3) {
                        val mapped = mapImageCategory(topPrediction.label)
                        ClassificationResult(
                            category = mapped.name,
                            confidence = topPrediction.confidence,
                            emoji = mapped.emoji,
                            details = "Изображение: ${topPrediction.label}"
                        )
                    } else {
                        ClassificationResult(
                            category = "Изображение",
                            confidence = 0.6f,
                            emoji = "🖼️",
                            details = "Графический файл"
                        )
                    }
                }

                is ImageClassifier.ClassificationResult.Error -> {
                    ClassificationResult(
                        category = "Изображение",
                        confidence = 0.5f,
                        emoji = "📷",
                        details = "Графический файл (анализ не удался)"
                    )
                }
            }
        }
    }

    /**
     * Классификация текста через BERT
     */
    private suspend fun classifyText(uri: Uri, context: Context, fileName: String): ClassificationResult {
        return withContext(Dispatchers.IO) {
            val result = textClassifier.classifyText(uri, context)

            when {
                result.success && result.category != null -> {
                    ClassificationResult(
                        category = result.category!!,
                        confidence = result.confidence ?: 0.7f,
                        emoji = result.emoji ?: "📄",
                        details = result.details ?: "Текстовый файл"
                    )
                }
                else -> {
                    ClassificationResult(
                        category = "Текстовый файл",
                        confidence = 0.7f,
                        emoji = "📄",
                        details = "Файл с текстовым содержимым"
                    )
                }
            }
        }
    }

    /**
     * Маппинг категорий изображений
     */
    private fun mapImageCategory(label: String): CategoryInfo {
        val lowerLabel = label.lowercase()

        return when {
            // Животные
            lowerLabel.contains("dog") || lowerLabel.contains("cat") ||
                    lowerLabel.contains("bird") || lowerLabel.contains("fish") ->
                CategoryInfo("Животное", "🐶")

            // Техника
            lowerLabel.contains("computer") || lowerLabel.contains("monitor") ||
                    lowerLabel.contains("keyboard") || lowerLabel.contains("phone") ->
                CategoryInfo("Техника", "💻")

            // Природа
            lowerLabel.contains("tree") || lowerLabel.contains("flower") ||
                    lowerLabel.contains("mountain") || lowerLabel.contains("sea") ->
                CategoryInfo("Природа", "🌳")

            // Еда
            lowerLabel.contains("food") || lowerLabel.contains("fruit") ||
                    lowerLabel.contains("pizza") || lowerLabel.contains("cake") ->
                CategoryInfo("Еда", "🍕")

            // Люди
            lowerLabel.contains("person") || lowerLabel.contains("man") ||
                    lowerLabel.contains("woman") || lowerLabel.contains("face") ->
                CategoryInfo("Люди", "👤")

            // Транспорт
            lowerLabel.contains("car") || lowerLabel.contains("bus") ||
                    lowerLabel.contains("plane") || lowerLabel.contains("bicycle") ->
                CategoryInfo("Транспорт", "🚗")

            // Текст/документы
            lowerLabel.contains("document") || lowerLabel.contains("paper") ||
                    lowerLabel.contains("book") || lowerLabel.contains("letter") ->
                CategoryInfo("Документ", "📄")

            else -> CategoryInfo("Изображение", "📷")
        }
    }

    /**
     * Получение расширения файла
     */
    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast(".", "").lowercase()
    }

    /**
     * Получение названия языка программирования
     */
    private fun getLanguageName(extension: String): String {
        return when (extension) {
            "java" -> "Java"
            "kt" -> "Kotlin"
            "py" -> "Python"
            "js" -> "JavaScript"
            "html" -> "HTML"
            "css" -> "CSS"
            "cpp" -> "C++"
            "c" -> "C"
            "php" -> "PHP"
            "swift" -> "Swift"
            else -> extension.uppercase()
        }
    }

    /**
     * Определение типа медиафайла
     */
    private fun getMediaType(extension: String): String {
        return when (extension) {
            "mp4", "avi", "mov", "mkv" -> "Видео"
            "mp3", "wav", "flac" -> "Аудио"
            else -> "Медиафайл"
        }
    }

    /**
     * Получение эмодзи для медиафайла
     */
    private fun getMediaEmoji(extension: String): String {
        return when (extension) {
            "mp4", "avi", "mov" -> "🎬"
            "mp3", "wav" -> "🎵"
            else -> "📁"
        }
    }

    /**
     * Проверка доступности классификаторов
     */
    fun areModelsAvailable(): Boolean {
        return imageClassifier.isAvailable() || textClassifier.isAvailable()
    }

    /**
     * Закрытие всех моделей
     */
    fun close() {
        imageClassifier.close()
        textClassifier.close()
    }

    data class CategoryInfo(
        val name: String,
        val emoji: String
    )

    data class ClassificationResult(
        val category: String,
        val confidence: Float,
        val emoji: String,
        val details: String
    )
}