package com.template.app.domain.model

import android.graphics.Bitmap

data class VelaHealth(
    val status: String,
    val uptimeSeconds: Long
)

data class VelaNetworkInfo(
    val localIp: String,
    val publicIp: String,
    val interfaceName: String
)

data class VelaAudioState(
    val volume: Int,
    val muted: Boolean
)

data class VelaMediaState(
    val title: String?,
    val artist: String?,
    val album: String?,
    val status: String?,
    val positionSeconds: Double?,
    val lengthSeconds: Double?,
    val artUrl: String? = null
)

data class VelaProcess(
    val pid: Int,
    val name: String,
    val cpu: Double,
    val mem: Double
)

data class VelaDiskUsage(
    val mountpoint: String,
    val total: String,
    val used: String,
    val free: String,
    val percent: Double
)

data class VelaNotification(
    val id: String,
    val title: String,
    val message: String,
    val appName: String?,
    val timestamp: Long
)

data class VelaWifiStatus(
    val connected: Boolean,
    val ssid: String?,
    val signal: Int?
)

data class VelaBrightness(
    val value: Int
)

data class VelaResolution(
    val width: Int,
    val height: Int,
    val refresh: Double,
    val output: String?
)

data class VelaCpuUsage(
    val overall: Double
)

data class VelaRamUsage(
    val percent: Double
)

data class VelaFileInfo(
    val name: String,
    val path: String,
    val type: String,
    val size: Long,
    val modified: Long
)

data class VelaClipboard(
    val content: String
)
