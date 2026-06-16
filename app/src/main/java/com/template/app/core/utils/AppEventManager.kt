package com.template.app.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object TriggerScreenshot : UiEvent()
    object TriggerLockScreen : UiEvent()
    object TriggerPlayPause : UiEvent()
}

@Singleton
class AppEventManager @Inject constructor() {
    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private val _isDashboardFabVisible = MutableStateFlow(false)
    val isDashboardFabVisible = _isDashboardFabVisible.asStateFlow()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun emitEvent(event: UiEvent) {
        scope.launch {
            _events.emit(event)
        }
    }

    fun showSnackbar(message: String) {
        emitEvent(UiEvent.ShowSnackbar(message))
    }

    fun setDashboardFabVisible(visible: Boolean) {
        _isDashboardFabVisible.value = visible
    }
}
