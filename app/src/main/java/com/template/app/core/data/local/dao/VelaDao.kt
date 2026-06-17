package com.template.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.template.app.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VelaDao {

    @Query("SELECT * FROM vela_health WHERE id = 0")
    fun observeHealth(): Flow<VelaHealthEntity?>

    @Upsert
    suspend fun upsertHealth(health: VelaHealthEntity)

    @Query("SELECT * FROM vela_network WHERE id = 0")
    fun observeNetwork(): Flow<VelaNetworkEntity?>

    @Upsert
    suspend fun upsertNetwork(network: VelaNetworkEntity)

    @Query("SELECT * FROM vela_audio WHERE id = 0")
    fun observeAudio(): Flow<VelaAudioEntity?>

    @Upsert
    suspend fun upsertAudio(audio: VelaAudioEntity)

    @Query("SELECT * FROM vela_audio_devices")
    fun observeAudioDevices(): Flow<List<VelaAudioDeviceEntity>>

    @Upsert
    suspend fun upsertAudioDevices(devices: List<VelaAudioDeviceEntity>)

    @Query("DELETE FROM vela_audio_devices")
    suspend fun clearAudioDevices()

    @Transaction
    suspend fun replaceAudioDevices(devices: List<VelaAudioDeviceEntity>) {
        clearAudioDevices()
        if (devices.isNotEmpty()) {
            upsertAudioDevices(devices)
        }
    }

    @Query("SELECT * FROM vela_media WHERE id = 0")
    fun observeMedia(): Flow<VelaMediaEntity?>

    @Upsert
    suspend fun upsertMedia(media: VelaMediaEntity)

    @Query("SELECT * FROM vela_processes ORDER BY cpu DESC LIMIT :limit")
    fun observeProcesses(limit: Int): Flow<List<VelaProcessEntity>>

    @Upsert
    suspend fun upsertProcesses(processes: List<VelaProcessEntity>)

    @Query("DELETE FROM vela_processes")
    suspend fun clearProcesses()

    @Query("DELETE FROM vela_processes WHERE pid NOT IN (:pids)")
    suspend fun deleteProcessesNotIn(pids: List<Int>)

    @Transaction
    suspend fun replaceProcesses(processes: List<VelaProcessEntity>) {
        if (processes.isEmpty()) {
            clearProcesses()
        } else {
            upsertProcesses(processes)
            deleteProcessesNotIn(processes.map { it.pid })
        }
    }

    @Query("SELECT * FROM vela_disks")
    fun observeDisks(): Flow<List<VelaDiskEntity>>

    @Upsert
    suspend fun upsertDisks(disks: List<VelaDiskEntity>)

    @Query("DELETE FROM vela_disks")
    suspend fun clearDisks()

    @Query("DELETE FROM vela_disks WHERE mountpoint NOT IN (:mountpoints)")
    suspend fun deleteDisksNotIn(mountpoints: List<String>)

    @Transaction
    suspend fun replaceDisks(disks: List<VelaDiskEntity>) {
        if (disks.isEmpty()) {
            clearDisks()
        } else {
            upsertDisks(disks)
            deleteDisksNotIn(disks.map { it.mountpoint })
        }
    }

    @Query("SELECT * FROM vela_notifications ORDER BY timestamp DESC")
    fun observeNotifications(): Flow<List<VelaNotificationEntity>>

    @Upsert
    suspend fun upsertNotifications(notifications: List<VelaNotificationEntity>)

    @Query("DELETE FROM vela_notifications")
    suspend fun clearNotifications()

    @Transaction
    suspend fun replaceNotifications(notifications: List<VelaNotificationEntity>) {
        clearNotifications()
        if (notifications.isNotEmpty()) {
            upsertNotifications(notifications)
        }
    }

    @Query("SELECT * FROM vela_wifi WHERE id = 0")
    fun observeWifi(): Flow<VelaWifiEntity?>

    @Upsert
    suspend fun upsertWifi(wifi: VelaWifiEntity)

    @Query("SELECT * FROM vela_brightness WHERE id = 0")
    fun observeBrightness(): Flow<VelaBrightnessEntity?>

    @Upsert
    suspend fun upsertBrightness(brightness: VelaBrightnessEntity)

    @Query("SELECT * FROM vela_resolution WHERE id = 0")
    fun observeResolution(): Flow<VelaResolutionEntity?>

    @Upsert
    suspend fun upsertResolution(resolution: VelaResolutionEntity)

    @Query("SELECT * FROM vela_cpu_usage WHERE id = 0")
    fun observeCpuUsage(): Flow<VelaCpuUsageEntity?>

    @Upsert
    suspend fun upsertCpuUsage(cpuUsage: VelaCpuUsageEntity)

    @Query("SELECT * FROM vela_ram_usage WHERE id = 0")
    fun observeRamUsage(): Flow<VelaRamUsageEntity?>

    @Upsert
    suspend fun upsertRamUsage(ramUsage: VelaRamUsageEntity)

    @Query("SELECT * FROM vela_clipboard WHERE id = 0")
    fun observeClipboard(): Flow<VelaClipboardEntity?>

    @Upsert
    suspend fun upsertClipboard(clipboard: VelaClipboardEntity)

    @Query("DELETE FROM vela_clipboard")
    suspend fun clearClipboard()

    @Query("SELECT * FROM vela_active_window WHERE id = 0")
    fun observeActiveWindow(): Flow<VelaActiveWindowEntity?>

    @Upsert
    suspend fun upsertActiveWindow(activeWindow: VelaActiveWindowEntity)

    @Query("SELECT * FROM vela_scheduled_tasks")
    fun observeScheduledTasks(): Flow<List<VelaScheduledTaskEntity>>

    @Upsert
    suspend fun upsertScheduledTasks(tasks: List<VelaScheduledTaskEntity>)

    @Query("DELETE FROM vela_scheduled_tasks")
    suspend fun clearScheduledTasks()

    @Query("DELETE FROM vela_scheduled_tasks WHERE id = :taskId")
    suspend fun deleteScheduledTask(taskId: String)

    @Transaction
    suspend fun replaceScheduledTasks(tasks: List<VelaScheduledTaskEntity>) {
        clearScheduledTasks()
        if (tasks.isNotEmpty()) {
            upsertScheduledTasks(tasks)
        }
    }

    @Query("SELECT * FROM vela_files WHERE parentPath = :parentPath")
    fun observeFiles(parentPath: String): Flow<List<VelaFileEntity>>

    @Upsert
    suspend fun upsertFiles(files: List<VelaFileEntity>)

    @Query("DELETE FROM vela_files WHERE parentPath = :parentPath")
    suspend fun clearFiles(parentPath: String)

    @Transaction
    suspend fun replaceFiles(parentPath: String, files: List<VelaFileEntity>) {
        clearFiles(parentPath)
        if (files.isNotEmpty()) {
            upsertFiles(files)
        }
    }
}
