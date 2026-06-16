package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.domain.model.VelaAudioDevice
import com.template.app.domain.model.VelaAudioState
import com.template.app.domain.repository.VelaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioUiState(
    val audioState: VelaAudioState? = null,
    val devices: List<VelaAudioDevice> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val velaRepository: VelaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeData()
        refresh()
    }

    private fun observeData() {
        velaRepository.observeAudio()
            .onEach { state -> _uiState.update { it.copy(audioState = state) } }
            .launchIn(viewModelScope)

        velaRepository.observeAudioDevices()
            .onEach { devices -> _uiState.update { it.copy(devices = devices) } }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            velaRepository.getVolume()
            velaRepository.getAudioDevices()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun setVolume(value: Int) {
        viewModelScope.launch {
            velaRepository.setVolume(value)
        }
    }

    fun volumeUp() {
        viewModelScope.launch {
            velaRepository.volumeUp()
        }
    }

    fun volumeDown() {
        viewModelScope.launch {
            velaRepository.volumeDown()
        }
    }

    fun toggleMute() {
        val currentState = _uiState.value.audioState ?: return
        viewModelScope.launch {
            velaRepository.setMute(!currentState.muted)
        }
    }

    fun toggleMicMute() {
        val currentState = _uiState.value.audioState ?: return
        viewModelScope.launch {
            velaRepository.setMicMute(!currentState.micMuted)
        }
    }

    fun selectDevice(device: VelaAudioDevice) {
        viewModelScope.launch {
            velaRepository.setOutputDevice(device.id)
            velaRepository.getAudioDevices() // Refresh to update selection state
        }
    }
}
