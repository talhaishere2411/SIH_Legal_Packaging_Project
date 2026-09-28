package com.legalmetrology.inspector.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// THEME — dark by default, with an app-wide light-mode toggle
// ============================================================

private fun darkColors() = darkColorScheme(
    // Primary (electric indigo)
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Indigo900,
    onPrimaryContainer = Indigo200,

    // Secondary (emerald — used for pass/success states)
    secondary = Emerald500,
    onSecondary = Navy900,
    secondaryContainer = Color(0xFF003D2E),
    onSecondaryContainer = Emerald400,

    // Error (coral — used for fail/violation states)
    error = Coral500,
    onError = Color.White,
    errorContainer = Color(0xFF4A0000),
    onErrorContainer = Coral400,

    // Tertiary (amber — used for pending/warning states)
    tertiary = Amber500,
    onTertiary = Navy900,
    tertiaryContainer = Color(0xFF3B2900),
    onTertiaryContainer = Amber400,

    // Backgrounds / Surfaces
    background = Navy900,
    onBackground = Gray100,
    surface = Navy800,
    onSurface = Gray100,
    surfaceVariant = Navy700,
    onSurfaceVariant = Gray300,
    surfaceTint = Indigo500,

    // Outline
    outline = Navy600,
    outlineVariant = Color(0xFF1F2937),

    // Inverse (for snackbars etc.)
    inverseSurface = Gray100,
    inverseOnSurface = Navy900,
    inversePrimary = Indigo700,

    // Scrim for dialogs
    scrim = Color(0xCC050812)
)

private fun lightColors() = lightColorScheme(
    // Keep the indigo brand accent while using dark text on light surfaces.
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EAF6),
    onPrimaryContainer = Color(0xFF1A1463),

    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8F4EC),
    onSecondaryContainer = Color(0xFF004D3D),

    error = Coral500,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    tertiary = Amber500,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE8B0),
    onTertiaryContainer = Color(0xFF3B2900),

    background = Navy900,
    onBackground = Gray100,
    surface = Navy800,
    onSurface = Gray100,
    surfaceVariant = Navy700,
    onSurfaceVariant = Gray300,
    surfaceTint = Indigo500,

    outline = Navy600,
    outlineVariant = Navy600,

    inverseSurface = Color(0xFF2D3038),
    inverseOnSurface = Color(0xFFF1F0F7),
    inversePrimary = Indigo200,

    scrim = Color(0x66000000)
)

@Composable
fun LegalMetrologyTheme(
    content: @Composable () -> Unit
) {
    // Reading ThemeMode here makes the entire Material tree recompose when
    // the user toggles the visual preference.
    val colorScheme = if (ThemeMode.isLight) lightColors() else darkColors()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
