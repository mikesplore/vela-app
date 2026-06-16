package com.template.app.domain.model

enum class AppThemeMode {
    LIGHT, DARK, SYSTEM
}

data class ConnectionSettings(
    val baseUrl: String = "",
    val apiToken: String = "",
    val onboardingComplete: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)
