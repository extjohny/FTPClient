package ru.abdulkhalikov.ftpclient.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class SimpleTextClassifier {

    companion object {
        private const val TAG = "SimpleTextClassifier"

        // Ключевые слова для определения категорий
        private val TECHNICAL_KEYWORDS = listOf(
            "код", "программа", "алгоритм", "база данных", "сервер", "функция",
            "класс", "объект", "интерфейс", "библиотека", "framework", "api",
            "git", "компиляция", "отладка", "тестирование", "deploy", "ci/cd"
        )

        private val BUSINESS_KEYWORDS = listOf(
            "отчет", "финанс", "бюджет", "прибыль", "компания", "корпорация",
            "акция", "инвестиция", "рынок", "стратегия", "план", "проект",
            "менеджмент", "лидерство", "презентация", "переговоры", "контракт"
        )

        private val LEGAL_KEYWORDS = listOf(
            "договор", "соглашение", "закон", "право", "статья", "кодекс",
            "суд", "иск", "жалоба", "адвокат", "нотариус", "лицензия",
            "патент", "авторское право", "конфиденциальность", "nda"
        )

        private val ACADEMIC_KEYWORDS = listOf(
            "исследование", "наука", "теория", "гипотеза", "эксперимент",
            "анализ", "методология", "публикация", "диссертация", "статья",
            "конференция", "симпозиум", "лаборатория", "университет"
        )

        private val NEWS_KEYWORDS = listOf(
            "новость", "событие", "политика", "экономика", "культура",
            "спорт", "технологии", "интервью", "репортаж", "комментарий",
            "аналитика", "прогноз", "тренд", "инновация"
        )

        private val PERSONAL_KEYWORDS = listOf(
            "я", "мне", "мой", "дневник", "заметка", "воспоминание",
            "идея", "мечта", "цель", "планы", "семья", "друзья",
            "путешествие", "хобби", "увлечение", "размышление"
        )
    }

    /**
     * Классифицирует текстовый файл по ключевым словам
     */
    fun classifyText(uri: Uri, context: Context): TextClassificationResult {
        return try {
            val text = readTextFromUri(uri, context, 5000)
            if (text.isBlank()) {
                return TextClassificationResult.error("Пустой текстовый файл")
            }

            // Анализ текста
            val analysisResult = analyzeTextContent(text)

            TextClassificationResult.success(
                category = analysisResult.category.displayName,
                confidence = analysisResult.confidence,
                emoji = analysisResult.category.emoji,
                details = analysisResult.details
            )
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка классификации текста: ${e.message}")
            TextClassificationResult.error("Ошибка анализа текста: ${e.message}")
        }
    }

    /**
     * Анализ текстового содержимого
     */
    private fun analyzeTextContent(text: String): TextAnalysisResult {
        val lowerText = text.lowercase()

        // Считаем совпадения для каждой категории
        val categoryScores = mapOf(
            TextCategory.TECHNICAL to countKeywords(lowerText, TECHNICAL_KEYWORDS),
            TextCategory.BUSINESS to countKeywords(lowerText, BUSINESS_KEYWORDS),
            TextCategory.LEGAL to countKeywords(lowerText, LEGAL_KEYWORDS),
            TextCategory.ACADEMIC to countKeywords(lowerText, ACADEMIC_KEYWORDS),
            TextCategory.NEWS to countKeywords(lowerText, NEWS_KEYWORDS),
            TextCategory.PERSONAL to countKeywords(lowerText, PERSONAL_KEYWORDS)
        )

        // Находим категорию с максимальным количеством совпадений
        val topCategory = categoryScores.maxByOrNull { it.value }

        return if (topCategory != null && topCategory.value > 0) {
            val confidence = calculateConfidence(topCategory.value, text.length)
            val details = buildDetails(text, topCategory.key, topCategory.value)

            TextAnalysisResult(
                category = topCategory.key,
                confidence = confidence,
                details = details
            )
        } else {
            // Если нет совпадений, определяем по структуре текста
            val fallbackCategory = determineByTextStructure(text)
            TextAnalysisResult(
                category = fallbackCategory,
                confidence = 0.5f,
                details = "Текст: ${text.take(100)}..."
            )
        }
    }

    /**
     * Подсчет ключевых слов в тексте
     */
    private fun countKeywords(text: String, keywords: List<String>): Int {
        return keywords.count { keyword ->
            text.contains(keyword, ignoreCase = true)
        }
    }

    /**
     * Расчет уверенности классификации
     */
    private fun calculateConfidence(keywordCount: Int, textLength: Int): Float {
        // Базовый расчет уверенности
        val density = keywordCount.toFloat() / (textLength / 1000f).coerceAtLeast(1f)
        return (0.5f + (density * 0.1f)).coerceAtMost(0.95f)
    }

    /**
     * Определение категории по структуре текста
     */
    private fun determineByTextStructure(text: String): TextCategory {
        val lines = text.lines()
        val words = text.split("\\s+".toRegex())

        return when {
            // Если много специальных символов - возможно код
            text.count { it in listOf('{', '}', '(', ')', ';', '=', '<', '>') } > 10 ->
                TextCategory.TECHNICAL

            // Если много цифр и дат - возможно отчет
            text.count { it.isDigit() } > text.length * 0.1 ->
                TextCategory.BUSINESS

            // Если текст короткий и содержит "я", "мне" - личный
            text.length < 500 && (text.contains(" я ") || text.contains("мне")) ->
                TextCategory.PERSONAL

            // По умолчанию - общий текст
            else -> TextCategory.GENERAL
        }
    }

    /**
     * Формирование деталей анализа
     */
    private fun buildDetails(text: String, category: TextCategory, keywordCount: Int): String {
        val sample = text.take(100).replace("\n", " ")
        return when (category) {
            TextCategory.TECHNICAL -> "Технический текст ($keywordCount совпадений): $sample..."
            TextCategory.BUSINESS -> "Бизнес-документ ($keywordCount совпадений): $sample..."
            TextCategory.LEGAL -> "Юридический документ ($keywordCount совпадений): $sample..."
            TextCategory.ACADEMIC -> "Научный текст ($keywordCount совпадений): $sample..."
            TextCategory.NEWS -> "Новость/статья ($keywordCount совпадений): $sample..."
            TextCategory.PERSONAL -> "Личная запись ($keywordCount совпадений): $sample..."
            TextCategory.GENERAL -> "Текст: $sample..."
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
                    line?.let {
                        text.append(it).append("\n")
                        totalChars += it.length
                    }
                }

                text.toString()
            } ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка чтения текста: ${e.message}")
            ""
        }
    }

    /**
     * Проверка доступности классификатора (всегда доступен)
     */
    fun isAvailable(): Boolean = true

    /**
     * Закрытие классификатора (ничего не делает)
     */
    fun close() {
        // Ничего не делаем, т.к. нет ресурсов для освобождения
    }

    /**
     * Категории текста
     */
    enum class TextCategory(val displayName: String, val emoji: String) {
        TECHNICAL("Технический текст", "⚙️"),
        BUSINESS("Бизнес-документ", "💼"),
        LEGAL("Юридический документ", "⚖️"),
        ACADEMIC("Научный текст", "🔬"),
        NEWS("Новость/Статья", "📰"),
        PERSONAL("Личная запись", "📝"),
        GENERAL("Текст", "📄")
    }

    /**
     * Результат анализа текста
     */
    data class TextAnalysisResult(
        val category: TextCategory,
        val confidence: Float,
        val details: String
    )

    /**
     * Результат классификации для внешнего использования
     */
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