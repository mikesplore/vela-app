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

    @Query("DELETE FROM vela_health")
    suspend fun clearHealth()

    @Query("SELECT * FROM vela_uptime WHERE id = 0")
    fun observeUptime(): Flow<VelaUptimeEntity?>

    @Upsert
    suspend fun upsertUptime(uptime: VelaUptimeEntity)

    @Query("SELECT * FROM NetUsageEntity WHERE id = 0")
    fun observeNetUsage(): Flow<NetUsageEntity?>

    @Upsert
    suspend fun upsertNetUsage(netUsage: NetUsageEntity)

    @Query("SELECT * FROM vela_device WHERE id = 0")
    fun observeDevice(): Flow<VelaDeviceEntity?>

    @Upsert
    suspend fun upsertDevice(device: VelaDeviceEntity)

    @Query("DELETE FROM vela_device")
    suspend fun clearDevice()

    @Query("SELECT * FROM vela_network WHERE id = 0")
    fun observeNetwork(): Flow<VelaNetworkEntity?>

    @Upsert
    suspend fun upsertNetwork(network: VelaNetworkEntity)

    @Query("DELETE FROM vela_network")
    fun clearNetwork()

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

    @Query("DELETE FROM vela_audio_devices WHERE id NOT IN (:ids)")
    suspend fun deleteAudioDevicesExcept(ids: List<String>)

    @Transaction
    suspend fun replaceAudioDevices(devices: List<VelaAudioDeviceEntity>) {
        if (devices.isEmpty()) {
            clearAudioDevices()
        } else {
            upsertAudioDevices(devices)
            deleteAudioDevicesExcept(devices.map { it.id })
        }
    }

    @Query("SELECT * FROM vela_media WHERE id = 0")
    fun observeMedia(): Flow<VelaMediaEntity?>

    @Upsert
    suspend fun upsertMedia(media: VelaMediaEntity)

    @Query("SELECT * FROM vela_processes WHERE isTopByMemory = 0 ORDER BY cpu DESC LIMIT :limit")
    fun observeProcesses(limit: Int): Flow<List<VelaProcessEntity>>

    @Query("SELECT * FROM vela_processes WHERE isTopByMemory = 1 ORDER BY mem DESC LIMIT :limit")
    fun observeProcessesByMemory(limit: Int): Flow<List<VelaProcessEntity>>

    @Upsert
    suspend fun upsertProcesses(processes: List<VelaProcessEntity>)

    @Query("DELETE FROM vela_processes")
    suspend fun clearProcesses()

    @Query("DELETE FROM vela_processes WHERE isTopByMemory = 0")
    suspend fun clearCpuProcesses()

    @Query("DELETE FROM vela_processes WHERE isTopByMemory = 1")
    suspend fun clearMemoryProcesses()

    @Query("DELETE FROM vela_processes WHERE isTopByMemory = 0 AND id NOT IN (:ids)")
    suspend fun deleteCpuProcessesExcept(ids: List<String>)

    @Query("DELETE FROM vela_processes WHERE isTopByMemory = 1 AND id NOT IN (:ids)")
    suspend fun deleteMemoryProcessesExcept(ids: List<String>)

    @Transaction
    suspend fun replaceCpuProcesses(processes: List<VelaProcessEntity>) {
        clearCpuProcesses()
        if (processes.isNotEmpty()) {
            upsertProcesses(processes)
        }
    }


    @Transaction
    suspend fun replaceMemoryProcesses(processes: List<VelaProcessEntity>) {
        clearMemoryProcesses()
        if (processes.isNotEmpty()) {
            upsertProcesses(processes)
        }
    }

    @Transaction
    suspend fun replaceProcesses(processes: List<VelaProcessEntity>) {
        if (processes.isEmpty()) {
            clearProcesses()
        } else {
            upsertProcesses(processes)

        }
    }

    @Query("SELECT * FROM vela_disks")
    fun observeDisks(): Flow<List<VelaDiskEntity>>

    @Upsert
    suspend fun upsertDisks(disks: List<VelaDiskEntity>)

    @Query("DELETE FROM vela_disks")
    suspend fun clearDisks()

    @Query("DELETE FROM vela_disks WHERE mountpoint NOT IN (:mountpoints)")
    suspend fun deleteDisksExcept(mountpoints: List<String>)

    @Transaction
    suspend fun replaceDisks(disks: List<VelaDiskEntity>) {
        if (disks.isEmpty()) {
            clearDisks()
        } else {
            upsertDisks(disks)
            deleteDisksExcept(disks.map { it.mountpoint })
        }
    }

    @Query("SELECT * FROM vela_notifications ORDER BY timestamp DESC")
    fun observeNotifications(): Flow<List<VelaNotificationEntity>>

    @Upsert
    suspend fun upsertNotifications(notifications: List<VelaNotificationEntity>)

    @Query("DELETE FROM vela_notifications")
    suspend fun clearNotifications()

    @Query("DELETE FROM vela_notifications WHERE id NOT IN (:ids)")
    suspend fun deleteNotificationsExcept(ids: List<String>)

    @Transaction
    suspend fun replaceNotifications(notifications: List<VelaNotificationEntity>) {
        if (notifications.isEmpty()) {
            clearNotifications()
        } else {
            upsertNotifications(notifications)
            deleteNotificationsExcept(notifications.map { it.id })
        }
    }

    // --- Network / Wifi / Bluetooth ---

    @Query("SELECT * FROM vela_wifi WHERE id = 0")
    fun observeWifi(): Flow<VelaWifiEntity?>

    @Upsert
    suspend fun upsertWifi(wifi: VelaWifiEntity)

    @Query("SELECT * FROM vela_wifi_networks")
    fun observeWifiNetworks(): Flow<List<VelaWifiNetworkEntity>>

    @Upsert
    suspend fun upsertWifiNetworks(networks: List<VelaWifiNetworkEntity>)

    @Query("DELETE FROM vela_wifi_networks")
    suspend fun clearWifiNetworks()

    @Query("DELETE FROM vela_wifi_networks WHERE ssid NOT IN (:ssids)")
    suspend fun deleteWifiNetworksExcept(ssids: List<String>)

    @Transaction
    suspend fun replaceWifiNetworks(networks: List<VelaWifiNetworkEntity>) {
        if (networks.isEmpty()) {
            clearWifiNetworks()
        } else {
            upsertWifiNetworks(networks)
            deleteWifiNetworksExcept(networks.map { it.ssid })
        }
    }

    @Query("SELECT * FROM vela_bluetooth WHERE id = 0")
    fun observeBluetoothState(): Flow<VelaBluetoothEntity?>

    @Upsert
    suspend fun upsertBluetoothState(state: VelaBluetoothEntity)

    @Query("SELECT * FROM vela_bluetooth_devices")
    fun observeBluetoothDevices(): Flow<List<VelaBluetoothDeviceEntity>>

    @Upsert
    suspend fun upsertBluetoothDevices(devices: List<VelaBluetoothDeviceEntity>)

    @Query("DELETE FROM vela_bluetooth_devices")
    suspend fun clearBluetoothDevices()

    @Query("DELETE FROM vela_bluetooth_devices WHERE address NOT IN (:addresses)")
    suspend fun deleteBluetoothDevicesExcept(addresses: List<String>)

    @Transaction
    suspend fun replaceBluetoothDevices(devices: List<VelaBluetoothDeviceEntity>) {
        if (devices.isEmpty()) {
            clearBluetoothDevices()
        } else {
            upsertBluetoothDevices(devices)
            deleteBluetoothDevicesExcept(devices.map { it.address })
        }
    }

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

    @Query("SELECT * FROM vela_gpu_usage")
    fun observeGpuUsage(): Flow<List<VelaGpuUsageEntity>>

    @Upsert
    suspend fun upsertGpuUsage(gpuUsage: List<VelaGpuUsageEntity>)

    @Query("DELETE FROM vela_gpu_usage")
    suspend fun clearGpuUsage()

    @Query("DELETE FROM vela_gpu_usage WHERE name NOT IN (:names)")
    suspend fun deleteGpuUsageExcept(names: List<String>)

    @Transaction
    suspend fun replaceGpuUsage(gpuUsage: List<VelaGpuUsageEntity>) {
        if (gpuUsage.isEmpty()) {
            clearGpuUsage()
        } else {
            upsertGpuUsage(gpuUsage)
            deleteGpuUsageExcept(gpuUsage.map { it.name })
        }
    }

    @Query("SELECT * FROM vela_disk_io")
    fun observeDiskIo(): Flow<List<VelaDiskIoEntity>>

    @Upsert
    suspend fun upsertDiskIo(diskIo: List<VelaDiskIoEntity>)

    @Query("DELETE FROM vela_disk_io")
    suspend fun clearDiskIo()

    @Query("DELETE FROM vela_disk_io WHERE device NOT IN (:devices)")
    suspend fun deleteDiskIoExcept(devices: List<String>)

    @Transaction
    suspend fun replaceDiskIo(diskIo: List<VelaDiskIoEntity>) {
        if (diskIo.isEmpty()) {
            clearDiskIo()
        } else {
            upsertDiskIo(diskIo)
            deleteDiskIoExcept(diskIo.map { it.device })
        }
    }

    @Query("SELECT * FROM vela_network_io")
    fun observeNetworkIo(): Flow<List<VelaNetworkIoEntity>>

    @Upsert
    suspend fun upsertNetworkIo(networkIo: List<VelaNetworkIoEntity>)

    @Query("DELETE FROM vela_network_io")
    suspend fun clearNetworkIo()

    @Query("DELETE FROM vela_network_io WHERE interfaceName NOT IN (:interfaces)")
    suspend fun deleteNetworkIoExcept(interfaces: List<String>)

    @Transaction
    suspend fun replaceNetworkIo(networkIo: List<VelaNetworkIoEntity>) {
        if (networkIo.isEmpty()) {
            clearNetworkIo()
        } else {
            upsertNetworkIo(networkIo)
            deleteNetworkIoExcept(networkIo.map { it.interfaceName })
        }
    }

    @Query("SELECT * FROM vela_temperatures")
    fun observeTemperatures(): Flow<List<VelaTemperatureEntity>>

    @Upsert
    suspend fun upsertTemperatures(temperatures: List<VelaTemperatureEntity>)

    @Query("DELETE FROM vela_temperatures")
    suspend fun clearTemperatures()

    @Query("DELETE FROM vela_temperatures WHERE id NOT IN (:ids)")
    suspend fun deleteTemperaturesExcept(ids: List<String>)

    @Transaction
    suspend fun replaceTemperatures(temperatures: List<VelaTemperatureEntity>) {
        if (temperatures.isEmpty()) {
            clearTemperatures()
        } else {
            upsertTemperatures(temperatures)
            deleteTemperaturesExcept(temperatures.map { it.id })
        }
    }

    @Query("SELECT * FROM vela_sensors")
    fun observeSensors(): Flow<List<VelaSensorEntity>>

    @Upsert
    suspend fun upsertSensors(sensors: List<VelaSensorEntity>)

    @Query("DELETE FROM vela_sensors")
    suspend fun clearSensors()

    @Query("DELETE FROM vela_sensors WHERE name NOT IN (:names)")
    suspend fun deleteSensorsExcept(names: List<String>)

    @Transaction
    suspend fun replaceSensors(sensors: List<VelaSensorEntity>) {
        if (sensors.isEmpty()) {
            clearSensors()
        } else {
            upsertSensors(sensors)
            deleteSensorsExcept(sensors.map { it.name })
        }
    }

    @Query("SELECT * FROM vela_fans")
    fun observeFans(): Flow<List<VelaFanEntity>>

    @Upsert
    suspend fun upsertFans(fans: List<VelaFanEntity>)

    @Query("DELETE FROM vela_fans")
    suspend fun clearFans()

    @Query("DELETE FROM vela_fans WHERE id NOT IN (:ids)")
    suspend fun deleteFansExcept(ids: List<String>)

    @Transaction
    suspend fun replaceFans(fans: List<VelaFanEntity>) {
        if (fans.isEmpty()) {
            clearFans()
        } else {
            upsertFans(fans)
            deleteFansExcept(fans.map { it.id })
        }
    }

    @Query("SELECT * FROM vela_battery WHERE id = 0")
    fun observeBattery(): Flow<VelaBatteryEntity?>

    @Upsert
    suspend fun upsertBattery(battery: VelaBatteryEntity)

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
        if (tasks.isEmpty()) {
            clearScheduledTasks()
        } else {
            upsertScheduledTasks(tasks)
            // Note: deleteScheduledTasksExcept might be needed here too
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
        if (files.isEmpty()) {
            clearFiles(parentPath)
        } else {
            upsertFiles(files)
            // Note: deleteFilesExcept might be needed here too
        }
    }

    @Query("SELECT * FROM vela_config WHERE id = 0")
    fun observeConfig(): Flow<VelaConfigEntity?>

    @Upsert
    suspend fun upsertConfig(config: VelaConfigEntity)
}
