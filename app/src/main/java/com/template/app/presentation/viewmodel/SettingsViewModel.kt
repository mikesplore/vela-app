package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.Resource
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.repository.VelaRepository
import com.template.app.domain.usecase.ClearSettingsUseCase
import com.template.app.domain.usecase.GetSettingsUseCase
import com.template.app.domain.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val baseUrl: String = "",
    val apiToken: String = "",
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isTesting: Boolean = false,
    val testResult: TestResult? = null,
    val isConnected: Boolean = false,
    val uptime: String = "",
    val agentVersion: String = "Unknown"
)

sealed interface TestResult {
    object Success : TestResult
    data class Error(val message: String) : TestResult
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val clearSettingsUseCase: ClearSettingsUseCase,
    private val velaRepository: VelaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getSettingsUseCase().collectLatest { settings ->
                _state.update { it.copy(
                    baseUrl = settings.baseUrl,
                    apiToken = settings.apiToken,
                    themeMode = settings.themeMode
                ) }
            }
        }

        viewModelScope.launch {
            velaRepository.observeHealth().collectLatest { health ->
                _state.update { it.copy(
                    isConnected = health != null,
                    uptime = health?.let { formatUptime(it.uptimeSeconds) } ?: "",
                    agentVersion = "Unknown"
                ) }
            }
        }
    }

    fun updateBaseUrl(url: String) {
        _state.update { it.copy(baseUrl = url, testResult = null) }
    }

    fun updateApiToken(token: String) {
        _state.update { it.copy(apiToken = token, testResult = null) }
    }

    fun updateTheme(mode: AppThemeMode) {
        viewModelScope.launch {
            saveSettingsUseCase.updateTheme(mode)
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _state.update { it.copy(isTesting = true, testResult = null) }
            
            saveSettingsUseCase(_state.value.baseUrl.trim(), _state.value.apiToken.trim(), _state.value.themeMode)

            when (val result = velaRepository.getHealth()) {
                is Resource.Success -> {
                    _state.update { it.copy(isTesting = false, testResult = TestResult.Success) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isTesting = false, testResult = TestResult.Error(result.message)) }
                }
                else -> {
                    _state.update { it.copy(isTesting = false) }
                }
            }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            saveSettingsUseCase(_state.value.baseUrl.trim(), _state.value.apiToken.trim(), _state.value.themeMode)
        }
    }

    fun clearCredentials(onComplete: () -> Unit) {
        viewModelScope.launch {
            clearSettingsUseCase()
            onComplete()
        }
    }

    private fun formatUptime(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        return if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
    }
}
