package com.template.app.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaResolution
import com.template.app.domain.repository.VelaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DisplayState(
    val screenshot: Bitmap? = null,
    val brightness: Int = 0,
    val resolution: VelaResolution? = null,
    val isNightLightEnabled: Boolean = false,
    val nightLightTemperature: Int = 4500, // Kelvin (1000 - 10000)
    val rotation: String = "normal",
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DisplayViewModel @Inject constructor(
    private val repository: VelaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DisplayState())
    val state: StateFlow<DisplayState> = _state.asStateFlow()

    init {
        observeData()
        refreshData()
    }

    private fun observeData() {
        repository.observeBrightness()
            .onEach { b -> 
                b?.let { _state.update { s -> s.copy(brightness = it.value) } } 
            }
            .launchIn(viewModelScope)

        repository.observeResolution()
            .onEach { res -> 
                res?.let { r ->
                    _state.update { s ->
                        s.copy(
                            resolution = r,
                            rotation = r.rotation,
                            isNightLightEnabled = r.nightLightEnabled,
                            nightLightTemperature = r.nightLightTemp
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshData() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            repository.getBrightness()
            repository.getResolution()
            takeScreenshot()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    fun takeScreenshot() {
        viewModelScope.launch {
            when (val result = repository.getScreenshot()) {
                is Resource.Success -> {
                    val base64Str = result.data
                    if (base64Str.isNotBlank()) {
                        val cleanBase64 = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
                        try {
                            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            _state.update { it.copy(screenshot = bitmap) }
                        } catch (e: Exception) {
                            _state.update { it.copy(error = "Failed to decode screenshot") }
                        }
                    }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun setBrightness(value: Int) {
        viewModelScope.launch {
            repository.setBrightness(value)
        }
    }

    fun rotate(orientation: String) {
        viewModelScope.launch {
            repository.rotateDisplay(orientation)
        }
    }

    fun setNightLight(enabled: Boolean, temperature: Int? = null) {
        viewModelScope.launch {
            val finalTemp = temperature ?: _state.value.nightLightTemperature
            repository.setNightLight(enabled, finalTemp)
        }
    }

    fun monitorOff() {
        viewModelScope.launch { repository.monitorOff() }
    }

    fun monitorOn() {
        viewModelScope.launch { repository.monitorOn() }
    }

    fun lockScreen() {
        viewModelScope.launch { repository.lockDisplay() }
    }

}
