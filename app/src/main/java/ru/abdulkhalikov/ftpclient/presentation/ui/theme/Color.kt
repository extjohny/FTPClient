package ru.abdulkhalikov.ftpclient.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// Основная палитра на основе предоставленных цветов
// Основные синие тона (из первой и третьей палитры)
val DeepBlue = Color(0xFF0A1931)    // Темно-синий фон
val OceanBlue = Color(0xFF1A3D63)   // Основной синий
val SkyBlue = Color(0xFF4A7FA7)     // Вторичный синий
val LightBlue = Color(0xFFB3CFE5)   // Акцентный голубой
val SnowWhite = Color(0xFFF6FAFD)   // Очень светлый фон

// Нейтральные тона (из второй палитры)
val Charcoal = Color(0xFF11212D)    // Темно-серый
val SlateGray = Color(0xFF253745)   // Серый
val SteelGray = Color(0xFF4A5C6A)   // Светло-серый
val Silver = Color(0xFF9BA8AB)      // Серебристый
val Platinum = Color(0xFFCCD0CF)    // Почти белый серый

// Google Material Design 3 цветовая палитра
// Light theme - светлая тема
val GoogleLightPrimary = OceanBlue          // #1A3D63 - глубокий синий
val GoogleLightOnPrimary = SnowWhite        // #F6FAFD - почти белый
val GoogleLightSecondary = SkyBlue          // #4A7FA7 - небесно-голубой
val GoogleLightOnSecondary = SnowWhite      // #F6FAFD
val GoogleLightTertiary = Color(0xFF34A853) // Google Green
val GoogleLightOnTertiary = SnowWhite
val GoogleLightBackground = SnowWhite       // #F6FAFD - очень светлый фон
val GoogleLightSurface = SnowWhite          // #F6FAFD
val GoogleLightOnSurface = DeepBlue         // #0A1931 - темно-синий для текста
val GoogleLightSurfaceVariant = Platinum    // #CCD0CF - светло-серый для карточек
val GoogleLightOnSurfaceVariant = SteelGray // #4A5C6A - серый текст
val GoogleLightError = Color(0xFFEA4335)    // Google Red
val GoogleLightOnError = SnowWhite

// Dark theme - темная тема
val GoogleDarkPrimary = LightBlue           // #B3CFE5 - светлый голубой
val GoogleDarkOnPrimary = DeepBlue          // #0A1931 - темно-синий
val GoogleDarkSecondary = Silver            // #9BA8AB - серебристый
val GoogleDarkOnSecondary = Charcoal        // #11212D - угольный
val GoogleDarkTertiary = Color(0xFF81C995)  // Google Green
val GoogleDarkOnTertiary = Charcoal
val GoogleDarkBackground = DeepBlue         // #0A1931 - темно-синий фон
val GoogleDarkSurface = Charcoal            // #11212D - темно-серый поверхность
val GoogleDarkOnSurface = LightBlue         // #B3CFE5 - светлый голубой текст
val GoogleDarkSurfaceVariant = SlateGray    // #253745 - серый вариант поверхности
val GoogleDarkOnSurfaceVariant = Silver     // #9BA8AB - серебристый текст
val GoogleDarkError = Color(0xFFF28B82)     // Google Red
val GoogleDarkOnError = DeepBlue

// Дополнительные цвета для элементов интерфейса
val SuccessGreen = Color(0xFF34A853)       // Google зеленый для успешных действий
val WarningOrange = Color(0xFFFBBC04)      // Google оранжевый для предупреждений
val InfoBlue = Color(0xFF4285F4)           // Google инфо-синий
val DisabledGray = SteelGray.copy(alpha = 0.5f) // Полупрозрачный серый для отключенных элементов

// Градиенты
val PrimaryGradient = listOf(GoogleLightPrimary, GoogleLightSecondary)
val SuccessGradient = listOf(SuccessGreen, Color(0xFF0F9D58))
val WarningGradient = listOf(WarningOrange, Color(0xFFFF9800))