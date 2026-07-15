package com.salahguard.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// SalahGuardTheme is the app's single MaterialTheme provider - called from
// MainActivity and AlarmActivity, nowhere else. It wires the raw palette
// (Color.kt) into a Material3 ColorScheme and the type scale (Type.kt) into
// MaterialTheme.typography. `SalahGuardShapes` (Shapes.kt) is available for
// a future ticket to wire in as `shapes = ` once existing dialogs/text
// fields/chips that rely on Material's default corner radii have been
// checked for visual regressions - see Ticket #002 migration notes.

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = NightBackground,
    secondary = GoldBright,
    onSecondary = NightBackground,
    tertiary = SuccessGreen,
    error = MissedRed,
    background = NightBackground,
    onBackground = WarmIvory,
    surface = NightSurface,
    onSurface = WarmIvory,
    surfaceVariant = NightSurfaceLighter,
    onSurfaceVariant = WarmStone,
    outline = MutedSand,
    outlineVariant = DisabledText
)

// Minimal light colors for alternate, prioritizing consistency in dark first
private val LightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Color.White,
    background = Color(0xFFF9F6F0),
    onBackground = Color(0xFF1A1C1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1A)
)

@Composable
fun SalahGuardTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = SalahGuardTypography,
        content = content
    )
}
