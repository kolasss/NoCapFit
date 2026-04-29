package dev.kolas.nocapfit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.kolas.nocapfit.data.preferences.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Indigo60,
    onPrimary = Gray100,
    primaryContainer = Indigo20,
    onPrimaryContainer = IndigoOnContainerDark,
    secondary = Indigo70,
    onSecondary = Gray05,
    secondaryContainer = Indigo30,
    onSecondaryContainer = Indigo90,
    tertiary = Amber60,
    onTertiary = Gray05,
    tertiaryContainer = Amber30,
    // Reuse tertiary so countdown text on the tinted container matches the timer accent.
    onTertiaryContainer = Amber60,
    error = Danger60,
    onError = Gray100,
    errorContainer = Danger20,
    onErrorContainer = Danger90,
    background = Gray05,
    onBackground = OnDarkText,
    surface = Gray15,
    onSurface = OnDarkText,
    surfaceVariant = Gray20,
    onSurfaceVariant = Gray70,
    outline = Gray50,
    outlineVariant = Gray25,
    inverseSurface = Gray96,
    inverseOnSurface = Gray15,
    inversePrimary = Indigo40,
    surfaceContainerLowest = Gray0,
    surfaceContainerLow = Gray10,
    surfaceContainer = Gray05,
    surfaceContainerHigh = Gray20,
    surfaceContainerHighest = Gray25
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo40,
    onPrimary = Gray100,
    primaryContainer = Indigo90,
    onPrimaryContainer = IndigoOnContainerLight,
    secondary = Indigo50,
    onSecondary = Gray100,
    secondaryContainer = Indigo90,
    onSecondaryContainer = Indigo30,
    tertiary = Amber50,
    onTertiary = Gray100,
    tertiaryContainer = Amber80,
    onTertiaryContainer = Amber50,
    error = Danger40,
    onError = Gray100,
    errorContainer = Danger90,
    onErrorContainer = Danger20,
    background = Gray96,
    onBackground = OnLightText,
    surface = Gray100,
    onSurface = OnLightText,
    surfaceVariant = Gray94,
    onSurfaceVariant = Gray60,
    outline = Gray70,
    outlineVariant = Gray90,
    inverseSurface = Gray15,
    inverseOnSurface = Gray96,
    inversePrimary = Indigo80,
    surfaceContainerLowest = Gray100,
    surfaceContainerLow = Gray98,
    surfaceContainer = Gray96,
    surfaceContainerHigh = Gray94,
    surfaceContainerHighest = Gray90
)

@Composable
fun NoCapFitTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor -> {
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
