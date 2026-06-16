package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.Resource
import com.template.app.domain.repository.VelaRepository
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
    private val repository: VelaRepository
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
            when (val result = repository.readClipboard()) {
                is Resource.Success -> {
                    // DB observer will update the content
                    _state.update { it.copy(isLoading = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun writeClipboard(text: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            when (val result = repository.writeClipboard(text)) {
                is Resource.Success -> {
                    _state.update { it.copy(isUpdating = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, isUpdating = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearClipboard() {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            when (val result = repository.clearClipboard()) {
                is Resource.Success -> {
                    _state.update { it.copy(isUpdating = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, isUpdating = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
