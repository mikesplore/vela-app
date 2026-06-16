package com.template.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.ConnectionSettings

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0, // Singleton pattern for settings
    val baseUrl: String,
    val apiToken: String,
    val onboardingComplete: Boolean,
    val themeMode: String = AppThemeMode.SYSTEM.name
) {
    fun toDomain() = ConnectionSettings(
        baseUrl = baseUrl,
        apiToken = apiToken,
        onboardingComplete = onboardingComplete,
        themeMode = AppThemeMode.valueOf(themeMode)
    )

    companion object {
        fun fromDomain(domain: ConnectionSettings) = SettingsEntity(
            baseUrl = domain.baseUrl,
            apiToken = domain.apiToken,
            onboardingComplete = domain.onboardingComplete,
            themeMode = domain.themeMode.name
        )
    }
}
