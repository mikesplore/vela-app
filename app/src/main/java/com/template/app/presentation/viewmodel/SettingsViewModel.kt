package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.VelaDevice
import com.template.app.domain.repository.HealthRepository
import com.template.app.domain.usecase.ClearSettingsUseCase
import com.template.app.domain.usecase.GetSettingsUseCase
import com.template.app.domain.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val device: VelaDevice? = null,
    val agentVersion: String = "Unknown"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val clearSettingsUseCase: ClearSettingsUseCase,
    private val velaRepository: HealthRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getSettingsUseCase().collectLatest { settings ->
                _state.update { it.copy(
                    themeMode = settings.themeMode
                ) }
            }
        }

        viewModelScope.launch {
            velaRepository.observeDevice().collectLatest { device ->
                _state.update { it.copy(device = device) }
            }
        }

        // Initial fetch
        refreshDevice()
    }

    fun refreshDevice() {
        viewModelScope.launch {
            velaRepository.getDevice()
        }
    }

    fun updateTheme(mode: AppThemeMode) {
        viewModelScope.launch {
            saveSettingsUseCase.updateTheme(mode)
        }
    }

    fun clearCredentials(onComplete: () -> Unit) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            clearSettingsUseCase()
            appEventManager.showActionSuccessSnackbar("Logged out successfully")
            onComplete()
            appEventManager.setLoading(false)
        }
    }
}
