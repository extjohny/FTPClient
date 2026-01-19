package ru.abdulkhalikov.ftpclient.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Если хотите использовать новые названия, замените так:
private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,           // вместо GoogleDarkPrimary
    onPrimary = DeepBlue,          // вместо GoogleDarkOnPrimary
    secondary = Silver,            // вместо GoogleDarkSecondary
    onSecondary = Charcoal,        // вместо GoogleDarkOnSecondary
    tertiary = Color(0xFF81C995),  // оставляем как было
    background = DeepBlue,         // вместо GoogleDarkBackground
    surface = Charcoal,            // вместо GoogleDarkSurface
    onSurface = LightBlue,         // вместо GoogleDarkOnSurface
    onBackground = LightBlue,      // вместо GoogleDarkOnSurface
    surfaceVariant = SlateGray,    // вместо GoogleDarkSurfaceVariant
    onSurfaceVariant = Silver,     // вместо GoogleDarkOnSurfaceVariant
    error = Color(0xFFF28B82),     // вместо GoogleDarkError
    onError = DeepBlue             // вместо GoogleDarkOnError
)

private val LightColorScheme = lightColorScheme(
    primary = OceanBlue,           // вместо GoogleLightPrimary
    onPrimary = SnowWhite,         // вместо GoogleLightOnPrimary
    secondary = SkyBlue,           // вместо GoogleLightSecondary
    onSecondary = SnowWhite,       // вместо GoogleLightOnSecondary
    tertiary = Color(0xFF34A853),  // оставляем как было
    background = SnowWhite,        // вместо GoogleLightBackground
    surface = SnowWhite,           // вместо GoogleLightSurface
    onSurface = DeepBlue,          // вместо GoogleLightOnSurface
    onBackground = DeepBlue,       // вместо GoogleLightOnSurface
    surfaceVariant = Platinum,     // вместо GoogleLightSurfaceVariant
    onSurfaceVariant = SteelGray,  // вместо GoogleLightOnSurfaceVariant
    error = Color(0xFFEA4335),     // вместо GoogleLightError
    onError = SnowWhite            // вместо GoogleLightOnError
)

@Composable
fun FTPClientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}