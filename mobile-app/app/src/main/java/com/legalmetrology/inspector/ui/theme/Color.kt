package com.legalmetrology.inspector.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// ============================================================
// LEGAL METROLOGY APP — DESIGN SYSTEM COLORS
// Palette: Deep navy base, electric indigo primary,
//          emerald success, coral failure, amber warning
//
// The dark palette remains the default. The light-mode values below
// intentionally keep the same brand accents while replacing the navy
// surfaces and pale text with accessible light surfaces and dark text.
// ============================================================

/** App-wide visual preference. It is deliberately UI-only and offline. */
object ThemeMode {
    var isLight by mutableStateOf(false)
        private set

    fun toggle() {
        isLight = !isLight
    }
}

private fun themed(dark: Color, light: Color): Color =
    if (ThemeMode.isLight) light else dark

// --- Brand / Primary ---
val Indigo900: Color get() = themed(Color(0xFF1A1463), Color(0xFFE8EAF6))
val Indigo700: Color get() = themed(Color(0xFF3730A3), Color(0xFF3730A3))
val Indigo500: Color get() = themed(Color(0xFF5C6BC0), Color(0xFF4F46B8))
val Indigo400: Color get() = themed(Color(0xFF7986CB), Color(0xFF4F46B8))
val Indigo200: Color get() = themed(Color(0xFFC5CAE9), Color(0xFF3730A3))

// --- Background / Surface ---
val Navy950: Color get() = themed(Color(0xFF050812), Color(0xFFF1F3F8))
val Navy900: Color get() = themed(Color(0xFF0A0E1A), Color(0xFFF8F9FC))
val Navy800: Color get() = themed(Color(0xFF111827), Color(0xFFFFFFFF))
val Navy700: Color get() = themed(Color(0xFF1F2937), Color(0xFFEEF1F7))
val Navy600: Color get() = themed(Color(0xFF374151), Color(0xFFD6DCE8))

// --- Status Colors ---
val Emerald500: Color get() = themed(Color(0xFF00C896), Color(0xFF008A6B))
val Emerald400: Color get() = themed(Color(0xFF34D399), Color(0xFF047857))
val Coral500: Color get() = themed(Color(0xFFFF5252), Color(0xFFD92D20))
val Coral400: Color get() = themed(Color(0xFFFF6E6E), Color(0xFFB42318))
val Amber500: Color get() = themed(Color(0xFFFBBF24), Color(0xFFA15C00))
val Amber400: Color get() = themed(Color(0xFFFCD34D), Color(0xFF8A4B00))
val Blue500: Color get() = themed(Color(0xFF3B82F6), Color(0xFF2563EB))

// --- Text ---
val White: Color get() = themed(Color(0xFFFFFFFF), Color(0xFF172033))
val Gray100: Color get() = themed(Color(0xFFF3F4F6), Color(0xFF172033))
val Gray300: Color get() = themed(Color(0xFFD1D5DB), Color(0xFF4B5563))
val Gray500: Color get() = themed(Color(0xFF6B7280), Color(0xFF6B7280))
val Gray700: Color get() = themed(Color(0xFF374151), Color(0xFF111827))

// --- AR Overlay Specific ---
val ArReticleTint: Color get() = themed(Color(0xFF00C896), Color(0xFF008A6B))
val ArSearchTint: Color get() = themed(Color(0xFF7986CB), Color(0xFF4F46B8))
val ArGlassPanel: Color get() = themed(Color(0x99111827), Color(0xD9FFFFFF))
