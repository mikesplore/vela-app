package com.template.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.template.app.domain.model.*

@Entity(tableName = "vela_health")
data class VelaHealthEntity(
    @PrimaryKey val id: Int = 0,
    val status: String,
    val uptimeSeconds: Long
) {
    fun toDomain() = VelaHealth(status, uptimeSeconds)
    companion object {
        fun fromDomain(domain: VelaHealth) = VelaHealthEntity(id = 0, status = domain.status, uptimeSeconds = domain.uptimeSeconds)
    }
}

@Entity(tableName = "vela_network")
data class VelaNetworkEntity(
    @PrimaryKey val id: Int = 0,
    val localIp: String,
    val publicIp: String,
    val interfaceName: String
) {
    fun toDomain() = VelaNetworkInfo(localIp, publicIp, interfaceName)
    companion object {
        fun fromDomain(domain: VelaNetworkInfo) = VelaNetworkEntity(
            id = 0,
            localIp = domain.localIp,
            publicIp = domain.publicIp,
            interfaceName = domain.interfaceName
        )
    }
}

@Entity(tableName = "vela_audio")
data class VelaAudioEntity(
    @PrimaryKey val id: Int = 0,
    val volume: Int,
    val muted: Boolean,
    val micMuted: Boolean = false,
    val activeDeviceId: String? = null
) {
    fun toDomain() = VelaAudioState(
        volume = volume, 
        muted = muted, 
        micMuted = micMuted,
        activeDeviceId = activeDeviceId
    )
    companion object {
        fun fromDomain(domain: VelaAudioState) = VelaAudioEntity(
            id = 0, 
            volume = domain.volume, 
            muted = domain.muted,
            micMuted = domain.micMuted,
            activeDeviceId = domain.activeDeviceId
        )
    }
}

@Entity(tableName = "vela_audio_devices")
data class VelaAudioDeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val isActive: Boolean
) {
    fun toDomain() = VelaAudioDevice(id, name, type, isActive)
    companion object {
        fun fromDomain(domain: VelaAudioDevice) = VelaAudioDeviceEntity(
            id = domain.id,
            name = domain.name,
            type = domain.type,
            isActive = domain.isActive
        )
    }
}

@Entity(tableName = "vela_media")
data class VelaMediaEntity(
    @PrimaryKey val id: Int = 0,
    val title: String?,
    val artist: String?,
    val album: String?,
    val status: String?,
    val positionSeconds: Double?,
    val lengthSeconds: Double?,
    val artUrl: String? = null
) {
    fun toDomain() = VelaMediaState(title, artist, album, status, positionSeconds, lengthSeconds, artUrl)
    companion object {
        fun fromDomain(domain: VelaMediaState) = VelaMediaEntity(
            id = 0,
            title = domain.title,
            artist = domain.artist,
            album = domain.album,
            status = domain.status,
            positionSeconds = domain.positionSeconds,
            lengthSeconds = domain.lengthSeconds,
            artUrl = domain.artUrl
        )
    }
}

@Entity(tableName = "vela_processes")
data class VelaProcessEntity(
    @PrimaryKey val pid: Int,
    val name: String,
    val cpu: Double,
    val mem: Double
) {
    fun toDomain() = VelaProcess(pid, name, cpu, mem)
    companion object {
        fun fromDomain(domain: VelaProcess) = VelaProcessEntity(
            pid = domain.pid,
            name = domain.name,
            cpu = domain.cpu,
            mem = domain.mem
        )
    }
}

@Entity(tableName = "vela_disks")
data class VelaDiskEntity(
    @PrimaryKey val mountpoint: String,
    val total: Long,
    val used: Long,
    val free: Long,
    val percent: Double
) {
    fun toDomain() = VelaDiskUsage(mountpoint, total.toString(), used.toString(), free.toString(), percent)
    companion object {
        fun fromDomain(domain: VelaDiskUsage) = VelaDiskEntity(
            mountpoint = domain.mountpoint,
            total = domain.total.toLongOrNull() ?: 0L,
            used = domain.used.toLongOrNull() ?: 0L,
            free = domain.free.toLongOrNull() ?: 0L,
            percent = domain.percent
        )
    }
}

@Entity(tableName = "vela_notifications")
data class VelaNotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val appName: String?,
    val timestamp: Long
) {
    fun toDomain() = VelaNotification(id, title, message, appName, timestamp)
    companion object {
        fun fromDomain(domain: VelaNotification) = VelaNotificationEntity(
            id = domain.id,
            title = domain.title,
            message = domain.message,
            appName = domain.appName,
            timestamp = domain.timestamp
        )
    }
}

@Entity(tableName = "vela_wifi")
data class VelaWifiEntity(
    @PrimaryKey val id: Int = 0,
    val connected: Boolean,
    val ssid: String?,
    val signal: Int?
) {
    fun toDomain() = VelaWifiStatus(connected, ssid, signal)
    companion object {
        fun fromDomain(domain: VelaWifiStatus) = VelaWifiEntity(
            id = 0,
            connected = domain.connected,
            ssid = domain.ssid,
            signal = domain.signal
        )
    }
}

@Entity(tableName = "vela_brightness")
data class VelaBrightnessEntity(
    @PrimaryKey val id: Int = 0,
    val value: Int
) {
    fun toDomain() = VelaBrightness(value)
    companion object {
        fun fromDomain(domain: VelaBrightness) = VelaBrightnessEntity(id = 0, value = domain.value)
    }
}

@Entity(tableName = "vela_resolution")
data class VelaResolutionEntity(
    @PrimaryKey val id: Int = 0,
    val width: Int,
    val height: Int,
    val refresh: Double,
    val output: String?,
    val rotation: String,
    val nightLightEnabled: Boolean,
    val nightLightTemp: Int
) {
    fun toDomain() = VelaResolution(width, height, refresh, output, rotation, nightLightEnabled, nightLightTemp)
    companion object {
        fun fromDomain(domain: VelaResolution) = VelaResolutionEntity(
            id = 0,
            width = domain.width,
            height = domain.height,
            refresh = domain.refresh,
            output = domain.output,
            rotation = domain.rotation,
            nightLightEnabled = domain.nightLightEnabled,
            nightLightTemp = domain.nightLightTemp
        )
    }
}

@Entity(tableName = "vela_cpu_usage")
data class VelaCpuUsageEntity(
    @PrimaryKey val id: Int = 0,
    val overall: Double
) {
    fun toDomain() = VelaCpuUsage(overall)
    companion object {
        fun fromDomain(domain: VelaCpuUsage) = VelaCpuUsageEntity(id = 0, overall = domain.overall)
    }
}

@Entity(tableName = "vela_ram_usage")
data class VelaRamUsageEntity(
    @PrimaryKey val id: Int = 0,
    val percent: Double
) {
    fun toDomain() = VelaRamUsage(percent)
    companion object {
        fun fromDomain(domain: VelaRamUsage) = VelaRamUsageEntity(id = 0, percent = domain.percent)
    }
}

@Entity(tableName = "vela_clipboard")
data class VelaClipboardEntity(
    @PrimaryKey val id: Int = 0,
    val content: String
) {
    companion object {
        fun fromContent(content: String) = VelaClipboardEntity(id = 0, content = content)
    }
}

@Entity(tableName = "vela_active_window")
data class VelaActiveWindowEntity(
    @PrimaryKey val id: Int = 0,
    val title: String
) {
    companion object {
        fun fromTitle(title: String) = VelaActiveWindowEntity(id = 0, title = title)
    }
}

@Entity(tableName = "vela_scheduled_tasks")
data class VelaScheduledTaskEntity(
    @PrimaryKey val id: String,
    val command: String,
    val nextRun: String,
    val recurring: String?
) {
    fun toDomain() = VelaScheduledTask(id, command, nextRun, recurring)
    companion object {
        fun fromDomain(domain: VelaScheduledTask) = VelaScheduledTaskEntity(
            id = domain.id,
            command = domain.command,
            nextRun = domain.nextRun,
            recurring = domain.recurring
        )
    }
}

@Entity(tableName = "vela_files")
data class VelaFileEntity(
    @PrimaryKey val path: String,
    val parentPath: String,
    val name: String,
    val type: String,
    val size: Long,
    val modified: Double,
    val isHidden: Boolean,
    val hasChildren: Boolean,
    val childrenCount: Int?,
    val extension: String?
) {
    fun toDomain() = VelaFileInfo(
        name = name, 
        path = path, 
        type = type, 
        size = size, 
        modified = modified,
        isHidden = isHidden,
        hasChildren = hasChildren,
        childrenCount = childrenCount,
        extension = extension
    )
    companion object {
        fun fromDomain(domain: VelaFileInfo, parentPath: String) = VelaFileEntity(
            path = domain.path,
            parentPath = parentPath,
            name = domain.name,
            type = domain.type,
            size = domain.size,
            modified = domain.modified,
            isHidden = domain.isHidden,
            hasChildren = domain.hasChildren,
            childrenCount = domain.childrenCount,
            extension = domain.extension
        )
    }
}
