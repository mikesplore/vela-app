package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface VelaRepository {
    // Observable streams (Offline-first)
    fun observeHealth(): Flow<VelaHealth?>
    fun observeNetwork(): Flow<VelaNetworkInfo?>
    fun observeAudio(): Flow<VelaAudioState?>
    fun observeAudioDevices(): Flow<List<VelaAudioDevice>>
    fun observeMedia(): Flow<VelaMediaState?>
    fun observeProcesses(limit: Int = 5): Flow<List<VelaProcess>>
    fun observeDisks(): Flow<List<VelaDiskUsage>>
    fun observeNotifications(): Flow<List<VelaNotification>>
    fun observeWifi(): Flow<VelaWifiStatus?>
    fun observeBrightness(): Flow<VelaBrightness?>
    fun observeResolution(): Flow<VelaResolution?>
    fun observeCpuUsage(): Flow<VelaCpuUsage?>
    fun observeRamUsage(): Flow<VelaRamUsage?>
    fun observeGpuUsage(): Flow<List<VelaGpuUsage>>
    fun observeDiskIo(): Flow<List<VelaDiskIo>>
    fun observeNetworkIo(): Flow<List<VelaNetworkIo>>
    fun observeTemperatures(): Flow<List<VelaTemperatureInfo>>
    fun observeFans(): Flow<List<VelaFanInfo>>
    fun observeSensors(): Flow<List<VelaSensorInfo>>
    fun observeBattery(): Flow<VelaBatteryStatus?>
    fun observeTopProcessesByCpu(limit: Int = 5): Flow<List<VelaProcess>>
    fun observeTopProcessesByMemory(limit: Int = 5): Flow<List<VelaProcess>>
    fun observeClipboard(): Flow<VelaClipboard?>
    fun observeActiveWindow(): Flow<String?>
    fun observeScheduledTasks(): Flow<List<VelaScheduledTask>>
    fun observeFiles(path: String): Flow<List<VelaFileInfo>>
    fun observeConfig(): Flow<VelaConfig?>


    // General
    suspend fun getHealth(): Resource<VelaHealth>
    
    // Display
    suspend fun getScreenshot(): Resource<String> // base64
    suspend fun setBrightness(value: Int): Resource<Unit>
    suspend fun lockDisplay(): Resource<Unit>
    suspend fun getResolution(): Resource<String>
    suspend fun getBrightness(): Resource<Int>
    suspend fun monitorOff(): Resource<Unit>
    suspend fun monitorOn(): Resource<Unit>
    suspend fun rotateDisplay(orientation: String): Resource<Unit>
    suspend fun setNightLight(enabled: Boolean, temperature: Int? = null): Resource<Unit>
    suspend fun recordDisplay(durationSeconds: Int): Resource<String> // base64
    
    // Audio
    suspend fun getVolume(): Resource<VelaAudioState>
    suspend fun setVolume(value: Int): Resource<VelaAudioState>
    suspend fun setMute(muted: Boolean): Resource<VelaAudioState>
    suspend fun volumeUp(step: Int = 5): Resource<VelaAudioState>
    suspend fun volumeDown(step: Int = 5): Resource<VelaAudioState>
    suspend fun getAudioDevices(): Resource<List<VelaAudioDevice>>
    suspend fun setOutputDevice(deviceId: String): Resource<Unit>
    suspend fun setMicMute(muted: Boolean): Resource<Unit>
    
    // Power
    suspend fun shutdown(): Resource<Unit>
    suspend fun restart(): Resource<Unit>
    suspend fun sleep(): Resource<Unit>
    suspend fun hibernate(): Resource<Unit>
    suspend fun scheduleShutdown(at: String): Resource<Unit>
    suspend fun cancelShutdown(): Resource<Unit>
    suspend fun getPowerProfile(): Resource<String>
    suspend fun setPowerProfile(profile: String): Resource<Unit>
    
    // Filesystem
    suspend fun listFiles(path: String?, showHidden: Boolean = false): Resource<VelaFileList>
    suspend fun getFileTree(path: String, maxDepth: Int = 1, showHidden: Boolean = false): Resource<VelaFileTree>
    suspend fun getDiskUsage(): Resource<List<VelaDiskUsage>>
    suspend fun downloadFile(path: String, destination: File): Resource<File>
    suspend fun uploadFile(path: String, file: File): Resource<Unit>
    suspend fun deleteFile(path: String): Resource<Unit>
    suspend fun makeDirectory(path: String): Resource<Unit>
    suspend fun renameFile(from: String, to: String): Resource<Unit>
    suspend fun searchFiles(query: String, path: String?): Resource<List<VelaFileInfo>>
    suspend fun zipFiles(paths: List<String>, output: String): Resource<Unit>
    suspend fun unzipFile(path: String, destination: String): Resource<Unit>
    suspend fun openFile(path: String): Resource<Unit>

    // Config
    suspend fun getConfig(): Resource<VelaConfig>
    suspend fun setConfig(config: VelaConfig): Resource<Unit>

    
    // Network
    suspend fun getNetworkInfo(): Resource<VelaNetworkInfo>
    suspend fun getNetworkLocation(): Resource<VelaNetworkInfo>
    suspend fun getWifiStatus(): Resource<VelaWifiStatus>
    suspend fun getWifiList(): Resource<List<VelaWifiNetwork>>
    suspend fun connectWifi(ssid: String, password: String): Resource<Unit>
    suspend fun disconnectWifi(): Resource<Unit>
    suspend fun toggleWifi(enabled: Boolean): Resource<Unit>
    suspend fun pingHost(host: String, count: Int): Resource<VelaPingResult>
    suspend fun runSpeedTest(): Resource<VelaSpeedTest>
    suspend fun getBluetoothDevices(): Resource<List<VelaBluetoothDevice>>
    suspend fun pairBluetooth(deviceId: String): Resource<Unit>
    suspend fun unpairBluetooth(deviceId: String): Resource<Unit>
    
    // Notifications
    suspend fun getNotifications(): Resource<List<VelaNotification>>
    
    // Clipboard
    suspend fun readClipboard(): Resource<String>
    suspend fun writeClipboard(text: String): Resource<Unit>
    suspend fun clearClipboard(): Resource<Unit>
    
    // Media
    suspend fun getNowPlaying(): Resource<VelaMediaState?>
    suspend fun togglePlayPause(): Resource<Unit>
    suspend fun mediaNext(): Resource<Unit>
    suspend fun mediaPrevious(): Resource<Unit>
    suspend fun mediaSeek(seconds: Int): Resource<Unit>
    
    // Processes
    suspend fun getProcesses(): Resource<List<VelaProcess>>
    suspend fun getActiveWindow(): Resource<String>
    suspend fun killProcess(pid: Int): Resource<Unit>

    // Monitor
    suspend fun getCpuUsage(): Resource<VelaCpuUsage>
    suspend fun getRamUsage(): Resource<VelaRamUsage>
    suspend fun getMonitorSnapshot(): Resource<VelaMonitorSnapshot>

    // Scheduler
    suspend fun getScheduledTasks(): Resource<List<VelaScheduledTask>>
    suspend fun createScheduledTask(command: String, runAt: String, recurring: String? = null): Resource<VelaScheduledTask>
    suspend fun cancelScheduledTask(taskId: String): Resource<Unit>
    suspend fun runTaskNow(taskId: String): Resource<Unit>

    // Maintenance
    suspend fun clearCache(): Resource<Unit>
    suspend fun getLogs(service: String, lines: Int): Resource<VelaLogs>
    suspend fun checkUpdates(): Resource<VelaMaintenanceUpdate>
    suspend fun runUpdates(): Resource<Unit>
    suspend fun syncTime(): Resource<Unit>
    suspend fun getServices(): Resource<List<VelaService>>
    suspend fun startService(name: String): Resource<Unit>
    suspend fun stopService(name: String): Resource<Unit>
    suspend fun restartService(name: String): Resource<Unit>
}
