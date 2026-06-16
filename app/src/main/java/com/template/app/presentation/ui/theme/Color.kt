package com.template.app.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// ── Vela Custom Palette ──────────────────────────────────────────────────────
val VelaBgDeep       = Color(0xFF070A10)
val VelaBgMid        = Color(0xFF0A0D14)
val VelaAccentIndigo = Color(0xFF6C63FF)
val VelaAccentCyan   = Color(0xFF00D9F5)
val VelaAccentRose   = Color(0xFFF43F5E)
val VelaTextPrimary  = Color(0xFFF0F4FF)
val VelaTextMuted    = Color(0xFF8B95A8)
val VelaCardBorder   = Color(0xFF1E2533)
val VelaWarning      = Color(0xFFFFB300)
val VelaSuccess      = Color(0xFF4CAF50)

// ── Light (Standard) ─────────────────────────────────────────────────────────
val md_theme_light_primary            = Color(0xFF006397)
val md_theme_light_onPrimary          = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer   = Color(0xFFCDE5FF)
val md_theme_light_secondary          = Color(0xFF50606E)
val md_theme_light_background         = Color(0xFFFCFCFF)
val md_theme_light_surface            = Color(0xFFFCFCFF)

// ── Dark (Vela-inspired) ─────────────────────────────────────────────────────
val md_theme_dark_primary             = VelaAccentIndigo
val md_theme_dark_onPrimary           = Color.White
val md_theme_dark_primaryContainer    = VelaAccentIndigo.copy(alpha = 0.2f)
val md_theme_dark_secondary           = VelaAccentCyan
val md_theme_dark_onSecondary         = Color.Black
val md_theme_dark_secondaryContainer  = VelaAccentCyan.copy(alpha = 0.2f)
val md_theme_dark_tertiary            = VelaWarning
val md_theme_dark_onTertiary          = Color.Black
val md_theme_dark_background          = VelaBgDeep
val md_theme_dark_onBackground        = VelaTextPrimary
val md_theme_dark_surface             = VelaBgMid
val md_theme_dark_onSurface           = VelaTextPrimary
val md_theme_dark_surfaceVariant      = VelaCardBorder
val md_theme_dark_onSurfaceVariant    = VelaTextMuted
val md_theme_dark_outline             = VelaCardBorder
val md_theme_dark_error               = VelaAccentRose
val md_theme_dark_onError             = Color.White
