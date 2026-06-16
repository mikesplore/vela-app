package com.template.app.domain.usecase

import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.ConnectionSettings
import com.template.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<ConnectionSettings> = repository.observeSettings()
}

class SaveSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(
        baseUrl: String,
        apiToken: String,
        themeMode: AppThemeMode = AppThemeMode.SYSTEM
    ) {
        val current = repository.getSettings()
        repository.saveSettings(
            current.copy(
                baseUrl = baseUrl,
                apiToken = apiToken,
                themeMode = themeMode
            )
        )
    }

    suspend fun updateTheme(themeMode: AppThemeMode) {
        val current = repository.getSettings()
        repository.saveSettings(current.copy(themeMode = themeMode))
    }
}

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() = repository.completeOnboarding()
}

class ClearSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() = repository.clearSettings()
}
