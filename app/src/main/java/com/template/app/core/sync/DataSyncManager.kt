package com.template.app.core.sync

import android.util.Log
import com.template.app.domain.repository.UserRepository
import com.template.app.domain.repository.VelaRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncManager @Inject constructor(
    private val userRepository: UserRepository,
    private val velaRepository: VelaRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null

    fun startSync() {
        if (syncJob?.isActive == true) return

        syncJob = scope.launch {
            while (isActive) {
                try {
                    Log.d("DataSyncManager", "Starting periodic sync...")
                    
                    // Run syncs in parallel
                    launch { userRepository.fetchUsers() }
                    launch { velaRepository.getHealth() }
                    launch { velaRepository.getCpuUsage() }
                    launch { velaRepository.getRamUsage() }
                    launch { velaRepository.getNowPlaying() }
                    launch { velaRepository.getNotifications() }
                    launch { velaRepository.getWifiStatus() }
                    launch { velaRepository.getVolume() }

                } catch (e: Exception) {
                    Log.e("DataSyncManager", "Sync failed", e)
                }
                delay(10_000) // 10 seconds
            }
        }
    }

    fun stopSync() {
        syncJob?.cancel()
    }
}
