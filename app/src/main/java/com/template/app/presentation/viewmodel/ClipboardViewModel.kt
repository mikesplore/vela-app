package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.repository.ClipboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClipboardState(
    val content: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false
)

@HiltViewModel
class ClipboardViewModel @Inject constructor(
    private val repository: ClipboardRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(ClipboardState())
    val state = _state.asStateFlow()

    init {
        observeClipboard()
        refresh()
    }

    private fun observeClipboard() {
        repository.observeClipboard()
            .onEach { clipboard ->
                _state.update { it.copy(content = clipboard?.content ?: "") }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.readClipboard()

            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to read clipboard")
                _state.update { it.copy(error = result.message) }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }


    fun writeClipboard(text: String) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _state.update { it.copy(isUpdating = true) }

            val result = repository.writeClipboard(text)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to write to clipboard")
            }

            // This must run regardless of success or error
            _state.update { it.copy(isUpdating = false) }
            appEventManager.setLoading(false)
        }
    }

    fun clearClipboard() {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _state.update { it.copy(isUpdating = true) }

            val result = repository.clearClipboard()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to clear clipboard")
            }

            // This must run regardless of success or error
            _state.update { it.copy(isUpdating = false) }
            appEventManager.setLoading(false)
        }
    }
}