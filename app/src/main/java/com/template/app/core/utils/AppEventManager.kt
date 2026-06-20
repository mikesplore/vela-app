package com.template.app.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID // Add this import
import javax.inject.Inject
import javax.inject.Singleton

sealed class UiEvent {
    data class ShowNetworkErrorSnackbar(val message: String) : UiEvent()
    data class ShowActionSuccessSnackbar(val message: String) : UiEvent()
    data class ShowActionErrorSnackbar(val message: String) : UiEvent()
}

data class NetworkErrorLog(
    // FIX: Use UUID for ID to prevent LazyColumn key collisions
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val method: String,
    val code: Int,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class AppEventManager @Inject constructor() {
    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isDashboardFabVisible = MutableStateFlow(false)
    val isDashboardFabVisible = _isDashboardFabVisible.asStateFlow()

    private val _networkLogs = MutableStateFlow<List<NetworkErrorLog>>(emptyList())
    val networkLogs = _networkLogs.asStateFlow()

    private val _errorCount = MutableStateFlow(0)
    val errorCount = _errorCount.asStateFlow()

    // FIX: Move pruning to Dispatchers.Default to avoid blocking the Main thread
    // if the list grows or during rapid updates
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            while (true) {
                delay(5000)
                pruneLogs()
            }
        }
    }

    fun emitEvent(event: UiEvent) {
        // Use a dedicated scope for events to ensure they are dispatched correctly
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            _events.emit(event)
        }
    }

    fun showNetworkErrorSnackbar(message: String) {
        emitEvent(UiEvent.ShowNetworkErrorSnackbar(message))
    }

    fun showActionSuccessSnackbar(message: String) {
        emitEvent(UiEvent.ShowActionSuccessSnackbar(message))
    }

    fun showActionErrorSnackbar(message: String) {
        emitEvent(UiEvent.ShowActionErrorSnackbar(message))
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setDashboardFabVisible(visible: Boolean) {
        _isDashboardFabVisible.value = visible
    }

    fun addNetworkErrorLog(url: String, method: String, code: Int, message: String) {
        val newLog = NetworkErrorLog(url = url, method = method, code = code, message = message)

        // Increment the count
        _errorCount.update { it + 1 }

        _networkLogs.update { currentLogs ->
            (listOf(newLog) + currentLogs).take(50)
        }
    }

    // Optional: Add a function to reset the count
    fun clearLogCount() {
        _errorCount.value = 0
    }

    private fun pruneLogs() {
        val now = System.currentTimeMillis()
        _networkLogs.update { logs ->
            logs.filter { now - it.timestamp < 30_000 }
        }
    }


}