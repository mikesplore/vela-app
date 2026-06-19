package com.template.app.core.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.*
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.model.VelaConfig
import com.template.app.domain.repository.VelaRepository
import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VelaRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val moshi: Moshi
) : VelaRepository {

    // Headstart cache for health status
    private val remoteHealth = MutableStateFlow<VelaHealth?>(null)

    override fun observeHealth(): Flow<VelaHealth?> =
        velaDao.observeHealth()
            .map { it?.toDomain() }
            .combine(remoteHealth) { local, remote ->
                // Prefer remote data if we have a fresh successful ping
                remote ?: local
            }
            .distinctUntilChanged()

    override fun observeNetwork(): Flow<VelaNetworkInfo?> =
        velaDao.observeNetwork().map { it?.toDomain() }

    override fun observeAudio(): Flow<VelaAudioState?> =
        velaDao.observeAudio().map { it?.toDomain() }

    override fun observeAudioDevices(): Flow<List<VelaAudioDevice>> =
        velaDao.observeAudioDevices().map { list -> list.map { it.toDomain() } }

    override fun observeMedia(): Flow<VelaMediaState?> =
        velaDao.observeMedia().map { it?.toDomain() }

    override fun observeProcesses(limit: Int): Flow<List<VelaProcess>> =
        velaDao.observeProcesses(limit).map { list -> list.map { it.toDomain() } }

    override fun observeDisks(): Flow<List<VelaDiskUsage>> =
        velaDao.observeDisks().map { list -> list.map { it.toDomain() } }

    override fun observeNotifications(): Flow<List<VelaNotification>> =
        velaDao.observeNotifications().map { list -> list.map { it.toDomain() } }

    override fun observeWifi(): Flow<VelaWifiStatus?> =
        velaDao.observeWifi().map { it?.toDomain() }

    override fun observeBrightness(): Flow<VelaBrightness?> =
        velaDao.observeBrightness().map { it?.toDomain() }

    override fun observeResolution(): Flow<VelaResolution?> =
        velaDao.observeResolution().map { it?.toDomain() }

    override fun observeCpuUsage(): Flow<VelaCpuUsage?> =
        velaDao.observeCpuUsage().map { it?.toDomain() }

    override fun observeRamUsage(): Flow<VelaRamUsage?> =
        velaDao.observeRamUsage().map { it?.toDomain() }

    override fun observeGpuUsage(): Flow<List<VelaGpuUsage>> =
        velaDao.observeGpuUsage().map { list -> list.map { it.toDomain() } }

    override fun observeDiskIo(): Flow<List<VelaDiskIo>> =
        velaDao.observeDiskIo().map { list -> list.map { it.toDomain() } }

    override fun observeNetworkIo(): Flow<List<VelaNetworkIo>> =
        velaDao.observeNetworkIo().map { list -> list.map { it.toDomain() } }

    override fun observeTemperatures(): Flow<List<VelaTemperatureInfo>> =
        velaDao.observeTemperatures().map { list -> list.map { it.toDomain() } }

    override fun observeFans(): Flow<List<VelaFanInfo>> =
        velaDao.observeFans().map { list -> list.map { it.toDomain() } }

    override fun observeSensors(): Flow<List<VelaSensorInfo>> =
        velaDao.observeSensors().map { list -> list.map { it.toDomain() } }

    override fun observeBattery(): Flow<VelaBatteryStatus?> =
        velaDao.observeBattery().map { it?.toDomain() }

    override fun observeTopProcessesByCpu(limit: Int): Flow<List<VelaProcess>> =
        velaDao.observeProcesses(limit).map { list -> list.map { it.toDomain() } }

    override fun observeTopProcessesByMemory(limit: Int): Flow<List<VelaProcess>> =
        velaDao.observeProcessesByMemory(limit).map { list -> list.map { it.toDomain() } }

    override fun observeClipboard(): Flow<VelaClipboard?> =
        velaDao.observeClipboard().map { it?.let { VelaClipboard(it.content) } }

    override fun observeActiveWindow(): Flow<String?> =
        velaDao.observeActiveWindow().map { it?.title }

    override fun observeScheduledTasks(): Flow<List<VelaScheduledTask>> =
        velaDao.observeScheduledTasks().map { list -> list.map { it.toDomain() } }

    override fun observeFiles(path: String): Flow<List<VelaFileInfo>> =
        velaDao.observeFiles(normalizePath(path)).map { list -> list.map { it.toDomain() } }

    override fun observeConfig(): Flow<VelaConfig?> =
        velaDao.observeConfig().map { it?.toDomain() }

    // --- Actions & Refreshing ---

    override suspend fun getHealth(): Resource<VelaHealth> = safeApiCall {
        try {
            val response = apiService.health()
            val domain = VelaHealth(
                status = response.status ?: "unknown",
                uptimeSeconds = response.uptimeSeconds ?: 0L
            )
            // Update headstart cache
            remoteHealth.value = domain
            // Update offline-first storage
            velaDao.upsertHealth(VelaHealthEntity.fromDomain(domain))
            domain
        } catch (e: Exception) {
            // If network fails, clear both headstart and cache so UI shows "Disconnected"
            remoteHealth.value = null
            velaDao.clearHealth()
            throw e
        }
    }

    override suspend fun getScreenshot(): Resource<String> = safeApiCall {
        apiService.getScreenshot().imageBase64 ?: ""
    }

    override suspend fun setBrightness(value: Int): Resource<Unit> = safeApiCall {
        apiService.setBrightness(BrightnessRequest(value))
        velaDao.upsertBrightness(VelaBrightnessEntity.fromDomain(VelaBrightness(value)))
        Unit
    }

    override suspend fun lockDisplay(): Resource<Unit> = safeApiCall {
        apiService.lockDisplay()
        Unit
    }

    override suspend fun getResolution(): Resource<String> = safeApiCall {
        val res = apiService.getResolution()
        val current = velaDao.observeResolution().firstOrNull()?.toDomain()
        val domain = VelaResolution(
            width = res.width ?: 0,
            height = res.height ?: 0,
            refresh = res.refresh ?: 0.0,
            output = res.output,
            rotation = current?.rotation ?: "normal",
            nightLightEnabled = current?.nightLightEnabled ?: false,
            nightLightTemp = current?.nightLightTemp ?: 4500
        )
        velaDao.upsertResolution(VelaResolutionEntity.fromDomain(domain))
        "${res.width}x${res.height} @ ${res.refresh}Hz"
    }

    override suspend fun monitorOff(): Resource<Unit> = safeApiCall {
        apiService.monitorOff()
        Unit
    }

    override suspend fun monitorOn(): Resource<Unit> = safeApiCall {
        apiService.monitorOn()
        Unit
    }

    override suspend fun rotateDisplay(orientation: String): Resource<Unit> = safeApiCall {
        apiService.rotateDisplay(RotateRequest(orientation))
        val current = velaDao.observeResolution().firstOrNull()?.toDomain()
        if (current != null) {
            velaDao.upsertResolution(VelaResolutionEntity.fromDomain(current.copy(rotation = orientation)))
        }
        Unit
    }

    override suspend fun setNightLight(enabled: Boolean, temperature: Int?): Resource<Unit> = safeApiCall {
        apiService.setNightLight(NightLightRequest(enabled, temperature))
        val current = velaDao.observeResolution().firstOrNull()?.toDomain()
        if (current != null) {
            velaDao.upsertResolution(VelaResolutionEntity.fromDomain(
                current.copy(
                    nightLightEnabled = enabled,
                    nightLightTemp = temperature ?: current.nightLightTemp
                )
            ))
        }
        Unit
    }

    override suspend fun recordDisplay(durationSeconds: Int): Resource<String> = safeApiCall {
        apiService.recordDisplay(RecordRequest(durationSeconds)).imageBase64 ?: ""
    }

    override suspend fun getVolume(): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.getVolume()
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0, 
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun setVolume(value: Int): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.setVolume(AudioVolumeRequest(value))
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0, 
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun setMute(muted: Boolean): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.setMute(AudioMuteRequest(muted))
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0, 
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun volumeUp(step: Int): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.volumeUp(AudioStepRequest(step))
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0, 
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun volumeDown(step: Int): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.volumeDown(AudioStepRequest(step))
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0, 
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun getAudioDevices(): Resource<List<VelaAudioDevice>> = safeApiCall {
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domains = apiService.getAudioDevices().map { 
            VelaAudioDevice(
                id = it.id ?: "",
                name = it.name ?: "Unknown Device",
                type = it.type ?: "unknown",
                isActive = it.id == current?.activeDeviceId
            )
        }
        velaDao.replaceAudioDevices(domains.map { VelaAudioDeviceEntity.fromDomain(it) })
        domains
    }

    override suspend fun setOutputDevice(deviceId: String): Resource<Unit> = safeApiCall {
        apiService.setOutputDevice(AudioOutputDeviceRequest(deviceId))
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        if (current != null) {
            velaDao.upsertAudio(VelaAudioEntity.fromDomain(current.copy(activeDeviceId = deviceId)))
        }
        Unit
    }

    override suspend fun setMicMute(muted: Boolean): Resource<Unit> = safeApiCall {
        if (muted) apiService.disableMic() else apiService.enableMic()
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        if (current != null) {
            velaDao.upsertAudio(VelaAudioEntity.fromDomain(current.copy(micMuted = muted)))
        }
        Unit
    }

    override suspend fun shutdown(): Resource<Unit> = safeApiCall {
        apiService.shutdown()
        Unit
    }

    override suspend fun restart(): Resource<Unit> = safeApiCall {
        apiService.restart()
        Unit
    }

    override suspend fun sleep(): Resource<Unit> = safeApiCall {
        apiService.sleep()
        Unit
    }

    override suspend fun hibernate(): Resource<Unit> = safeApiCall {
        apiService.hibernate()
        Unit
    }

    override suspend fun scheduleShutdown(at: String): Resource<Unit> = safeApiCall {
        apiService.scheduleShutdown(ScheduleShutdownRequest(at))
        Unit
    }

    override suspend fun cancelShutdown(): Resource<Unit> = safeApiCall {
        apiService.cancelShutdown(ScheduleShutdownRequest("now"))
        Unit
    }

    override suspend fun getPowerProfile(): Resource<String> = safeApiCall {
        apiService.getPowerProfile().profile ?: "unknown"
    }

    override suspend fun setPowerProfile(profile: String): Resource<Unit> = safeApiCall {
        apiService.setPowerProfile(PowerProfileRequest(profile))
        Unit
    }

    // --- Filesystem ---

    override suspend fun getConfig(): Resource<VelaConfig> = safeApiCall {
        val response = apiService.getConfig()
        val domain = VelaConfig(
            homeDirectory = response.homeDirectory,
            username = response.username
        )
        velaDao.upsertConfig(VelaConfigEntity.fromDomain(domain))
        domain
    }

    override suspend fun setConfig(config: VelaConfig): Resource<Unit> = safeApiCall {
        velaDao.upsertConfig(VelaConfigEntity.fromDomain(config))
        Unit
    }

    override suspend fun listFiles(path: String?, showHidden: Boolean): Resource<VelaFileList> = safeApiCall {
        val normalizedReqPath = normalizePath(path ?: "")
        val response = apiService.listFiles(normalizedReqPath, showHidden)
        val fileDomains = response.files?.map { it.toDomain() } ?: emptyList()
        
        val currentPath = normalizePath(response.currentPath ?: normalizedReqPath)
        
        val domain = VelaFileList(
            currentPath = currentPath,
            parentPath = response.parentPath?.let { normalizePath(it) },
            totalItems = response.totalItems ?: fileDomains.size,
            showHidden = response.showHidden ?: showHidden,
            files = fileDomains
        )
        
        velaDao.replaceFiles(currentPath, fileDomains.map { VelaFileEntity.fromDomain(it, currentPath) })
        domain
    }

    override suspend fun getFileTree(path: String, maxDepth: Int, showHidden: Boolean): Resource<VelaFileTree> = safeApiCall {
        val normalizedPath = normalizePath(path)
        val response = apiService.getTree(normalizedPath, maxDepth, showHidden)
        VelaFileTree(
            root = response.root?.toDomain() ?: VelaFileInfo("", normalizedPath, "directory", 0L, 0.0),
            children = response.children?.map { it.toDomain() } ?: emptyList(),
            breadcrumbs = response.breadcrumbs?.map { VelaBreadcrumb(it.name ?: "", normalizePath(it.path ?: "")) } ?: emptyList()
        )
    }

    override suspend fun getDiskUsage(): Resource<List<VelaDiskUsage>> = safeApiCall {
        val domains = apiService.getDiskUsage().usage?.map {
            VelaDiskUsage(
                mountpoint = it.mountpoint ?: "",
                total = it.total ?: "0",
                used = it.used?: "0",
                free = it.free ?: "0",
                percent = it.percent ?: 0.0
            )
        } ?: emptyList()
        velaDao.replaceDisks(domains.map { VelaDiskEntity.fromDomain(it) })
        domains
    }

    override suspend fun downloadFile(path: String, destination: File): Resource<File> = safeApiCall {
        val body = apiService.downloadFile(path)
        body.byteStream().use { inputStream ->
            destination.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        destination
    }

    override suspend fun uploadFile(path: String, file: File): Resource<Unit> = safeApiCall {
        val pathBody = path.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        val multipart = MultipartBody.Part.createFormData("file", file.name, fileBody)
        apiService.uploadFile(pathBody, multipart)
        Unit
    }

    override suspend fun deleteFile(path: String): Resource<Unit> = safeApiCall {
        apiService.deleteFile(FilePathRequest(path))
        Unit
    }

    override suspend fun makeDirectory(path: String): Resource<Unit> = safeApiCall {
        apiService.makeDirectory(FilePathRequest(path))
        Unit
    }

    override suspend fun renameFile(from: String, to: String): Resource<Unit> = safeApiCall {
        apiService.renameFile(FileRenameRequest(from, to))
        Unit
    }

    override suspend fun searchFiles(query: String, path: String?): Resource<List<VelaFileInfo>> = safeApiCall {
        apiService.searchFiles(query, path).files?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun zipFiles(paths: List<String>, output: String): Resource<Unit> = safeApiCall {
        apiService.zipFiles(ZipRequest(paths, output))
        Unit
    }

    override suspend fun unzipFile(path: String, destination: String): Resource<Unit> = safeApiCall {
        apiService.unzipFile(UnzipRequest(path, destination))
        Unit
    }

    override suspend fun openFile(path: String): Resource<Unit> = safeApiCall {
        apiService.openFile(FilePathRequest(path))
        Unit
    }

    // --- Network ---

    override suspend fun getNetworkInfo(): Resource<VelaNetworkInfo> = safeApiCall {
        val response = apiService.getNetworkIp()
        val domain = VelaNetworkInfo(
            localIp = response.localIp ?: "",
            publicIp = response.publicIp ?: "",
            interfaceName = response.interfaceName ?: ""
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(domain))
        domain
    }

    override suspend fun getNetworkLocation(): Resource<VelaNetworkInfo> = safeApiCall {
        val response = apiService.getNetworkLocation()
        val domain = VelaNetworkInfo(
            localIp = response.localIp ?: "",
            publicIp = response.publicIp ?: "",
            interfaceName = "",
            location = response.location?.let {
                VelaLocation(it.country, it.city, it.lat, it.lon)
            }
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(domain))
        domain
    }

    override suspend fun getWifiStatus(): Resource<VelaWifiStatus> = safeApiCall {
        val res = apiService.getWifiStatus()
        val domain = VelaWifiStatus(
            connected = res.connected ?: false,
            ssid = res.ssid,
            signal = res.signal,
            availableNetworks = res.networks?.map { VelaWifiNetwork(it.ssid ?: "Unknown", it.signal ?: 0) } ?: emptyList()
        )
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(domain))
        domain
    }

    override suspend fun getWifiList(): Resource<List<VelaWifiNetwork>> = safeApiCall {
        apiService.getWifiList().networks?.map {
            VelaWifiNetwork(it.ssid ?: "Unknown", it.signal ?: 0)
        } ?: emptyList()
    }

    override suspend fun connectWifi(ssid: String, password: String): Resource<Unit> = safeApiCall {
        apiService.connectWifi(WifiConnectRequest(ssid, password))
        Unit
    }

    override suspend fun disconnectWifi(): Resource<Unit> = safeApiCall {
        apiService.disconnectWifi()
        Unit
    }

    override suspend fun toggleWifi(enabled: Boolean): Resource<Unit> = safeApiCall {
        apiService.toggleWifi(WifiToggleRequest(enabled))
        Unit
    }

    override suspend fun pingHost(host: String, count: Int): Resource<VelaPingResult> = safeApiCall {
        val res = apiService.pingHost(PingHostRequest(host, count))
        VelaPingResult(
            host = res.host ?: host,
            lossPercent = res.packetLoss ?: 0.0,
            avgRttMs = res.avgRttMs ?: 0.0,
            transmitted = res.packetsTransmitted ?: 0,
            received = res.packetsReceived ?: 0
        )
    }

    override suspend fun runSpeedTest(): Resource<VelaSpeedTest> = safeApiCall {
        val res = apiService.speedTest()
        VelaSpeedTest(
            downloadMbps = res.downloadMbps ?: 0.0,
            uploadMbps = res.uploadMbps ?: 0.0,
            pingMs = res.pingMs ?: 0.0
        )
    }

    override suspend fun getBluetoothDevices(): Resource<List<VelaBluetoothDevice>> = safeApiCall {
        apiService.getBluetoothDevices().devices?.map {
            VelaBluetoothDevice(it.id ?: "", it.name ?: "Unknown", it.paired ?: false)
        } ?: return@safeApiCall emptyList()
    }

    override suspend fun pairBluetooth(deviceId: String): Resource<Unit> = safeApiCall {
        apiService.pairBluetooth(BluetoothDeviceRequest(deviceId))
        Unit
    }

    override suspend fun unpairBluetooth(deviceId: String): Resource<Unit> = safeApiCall {
        apiService.unpairBluetooth(BluetoothDeviceRequest(deviceId))
        Unit
    }

    override suspend fun getNotifications(): Resource<List<VelaNotification>> = safeApiCall {
        val domains = apiService.getNotifications().notifications?.map {
            VelaNotification(
                id = it.id?.toString() ?: "",
                title = it.title ?: "",
                message = it.message ?: "",
                appName = it.appName,
                timestamp = System.currentTimeMillis()
            )
        } ?: emptyList()
        velaDao.replaceNotifications(domains.map { VelaNotificationEntity.fromDomain(it) })
        domains
    }

    override suspend fun readClipboard(): Resource<String> = safeApiCall {
        val data = apiService.readClipboard().data ?: ""
        velaDao.upsertClipboard(VelaClipboardEntity.fromContent(data))
        data
    }

    override suspend fun writeClipboard(text: String): Resource<Unit> = safeApiCall {
        apiService.writeClipboard(ClipboardWriteRequest(text))
        velaDao.upsertClipboard(VelaClipboardEntity.fromContent(text))
        Unit
    }

    override suspend fun clearClipboard(): Resource<Unit> = safeApiCall {
        apiService.clearClipboard()
        velaDao.clearClipboard()
        Unit
    }

    override suspend fun getNowPlaying(): Resource<VelaMediaState?> = safeApiCall {
        apiService.getNowPlaying().let {
            val domain = VelaMediaState(
                title = it.title,
                artist = it.artist,
                album = it.album,
                status = it.status,
                positionSeconds = it.positionSeconds,
                lengthSeconds = it.lengthSeconds,
                artUrl = it.artUrl
            )
            velaDao.upsertMedia(VelaMediaEntity.fromDomain(domain))
            domain
        }
    }

    override suspend fun togglePlayPause(): Resource<Unit> = safeApiCall {
        apiService.togglePlayPause()
        Unit
    }

    override suspend fun mediaNext(): Resource<Unit> = safeApiCall {
        apiService.mediaNext()
        Unit
    }

    override suspend fun mediaPrevious(): Resource<Unit> = safeApiCall {
        apiService.mediaPrevious()
        Unit
    }

    override suspend fun mediaSeek(seconds: Int): Resource<Unit> = safeApiCall {
        apiService.mediaSeek(MediaSeekRequest(seconds))
        Unit
    }

    override suspend fun getProcesses(): Resource<List<VelaProcess>> = safeApiCall {
        val jsonStr = apiService.getProcesses().string()
        val domains = parseProcessesResiliently(jsonStr)
        velaDao.replaceProcesses(domains.map { VelaProcessEntity.fromDomain(it) })
        domains
    }

    override suspend fun getActiveWindow(): Resource<String> = safeApiCall {
        val title = apiService.getActiveWindow().title ?: ""
        velaDao.upsertActiveWindow(VelaActiveWindowEntity.fromTitle(title))
        title
    }

    override suspend fun killProcess(pid: Int): Resource<Unit> = safeApiCall {
        apiService.killProcessByPid(pid)
        Unit
    }

    override suspend fun getBrightness(): Resource<Int> = safeApiCall {
        val brightness = apiService.getBrightness().brightness?.toInt() ?: 0
        velaDao.upsertBrightness(VelaBrightnessEntity.fromDomain(VelaBrightness(brightness)))
        brightness
    }

    override suspend fun getCpuUsage(): Resource<VelaCpuUsage> = safeApiCall {
        val res = apiService.getMonitorCpu()
        val domain = VelaCpuUsage(res.overall ?: 0.0, res.perCore ?: emptyList())
        velaDao.upsertCpuUsage(VelaCpuUsageEntity.fromDomain(domain))
        domain
    }

    override suspend fun getRamUsage(): Resource<VelaRamUsage> = safeApiCall {
        val res = apiService.getMonitorRam()
        val domain = VelaRamUsage(
            total = res.total ?: 0,
            available = res.available ?: 0,
            used = res.used ?: 0,
            percent = res.percent ?: 0.0,
            swapTotal = res.swapTotal ?: 0,
            swapUsed = res.swapUsed ?: 0,
            swapFree = res.swapFree ?: 0,
            swapPercent = res.swapPercent ?: 0.0
        )
        velaDao.upsertRamUsage(VelaRamUsageEntity.fromDomain(domain))
        domain
    }

    override suspend fun getMonitorSnapshot(): Resource<VelaMonitorSnapshot> = safeApiCall {
        val res = apiService.getMonitorSnapshot()
        
        val cpuDomain = VelaCpuUsage(res.cpu?.overall ?: 0.0, res.cpu?.perCore ?: emptyList())
        val ramDomain = VelaRamUsage(
            total = res.ram?.total ?: 0,
            available = res.ram?.available ?: 0,
            used = res.ram?.used ?: 0,
            percent = res.ram?.percent ?: 0.0,
            swapTotal = res.ram?.swapTotal ?: 0,
            swapUsed = res.ram?.swapUsed ?: 0,
            swapFree = res.ram?.swapFree ?: 0,
            swapPercent = res.ram?.swapPercent ?: 0.0
        )
        val gpuDomains = res.gpu?.map { 
            VelaGpuUsage(it.name, it.usagePercent ?: 0.0, it.vramTotal ?: 0, it.vramUsed ?: 0, it.vramPercent ?: 0.0)
        } ?: emptyList()
        val diskIoDomains = res.diskIo?.map { 
            VelaDiskIo(it.device ?: "unknown", it.readBytesPerSec ?: 0.0, it.writeBytesPerSec ?: 0.0)
        } ?: emptyList()
        val networkIoDomains = res.networkIo?.map { 
            VelaNetworkIo(it.interfaceName ?: "unknown", it.bytesSentPerSec ?: 0.0, it.bytesRecvPerSec ?: 0.0)
        } ?: emptyList()
        val tempDomains = res.temperatures?.map { 
            VelaTemperatureInfo(it.sensor ?: "unknown", it.label ?: "", it.current ?: 0.0, it.high, it.critical)
        } ?: emptyList()
        val fanDomains = res.fans?.mapIndexed { index, it -> 
            VelaFanInfo(it.sensor ?: "unknown", it.speedRpm ?: 0, index)
        } ?: emptyList()
        val batteryDomain = res.battery?.let { 
            VelaBatteryStatus(it.percent ?: 0.0, it.pluggedIn ?: false, it.secsLeft)
        }
        val cpuProcDomains = res.processes?.topByCpu?.map { it.toDomain() } ?: emptyList()
        val memProcDomains = res.processes?.topByMemory?.map { it.toDomain() } ?: emptyList()

        // generic sensors could be added here if available in snapshot
        val sensorDomains = emptyList<VelaSensorInfo>()

        // Sync to Room
        velaDao.upsertCpuUsage(VelaCpuUsageEntity.fromDomain(cpuDomain))
        velaDao.upsertRamUsage(VelaRamUsageEntity.fromDomain(ramDomain))
        velaDao.replaceGpuUsage(gpuDomains.map { VelaGpuUsageEntity.fromDomain(it) })
        velaDao.replaceDiskIo(diskIoDomains.map { VelaDiskIoEntity.fromDomain(it) })
        velaDao.replaceNetworkIo(networkIoDomains.map { VelaNetworkIoEntity.fromDomain(it) })
        velaDao.replaceTemperatures(tempDomains.map { VelaTemperatureEntity.fromDomain(it) })
        velaDao.replaceFans(fanDomains.map { VelaFanEntity.fromDomain(it) })
        velaDao.replaceSensors(sensorDomains.map { VelaSensorEntity.fromDomain(it) })
        
        if (batteryDomain != null) velaDao.upsertBattery(VelaBatteryEntity.fromDomain(batteryDomain))
        velaDao.replaceCpuProcesses(cpuProcDomains.map { VelaProcessEntity.fromDomain(it, isTopByMemory = false) })
        velaDao.replaceMemoryProcesses(memProcDomains.map { VelaProcessEntity.fromDomain(it, isTopByMemory = true) })

        VelaMonitorSnapshot(
            cpu = cpuDomain,
            ram = ramDomain,
            gpu = gpuDomains,
            diskIo = diskIoDomains,
            networkIo = networkIoDomains,
            temperatures = tempDomains,
            fans = fanDomains,
            battery = batteryDomain,
            topProcessesByCpu = cpuProcDomains,
            topProcessesByMemory = memProcDomains,
            sensors = sensorDomains
        )
    }

    override suspend fun getScheduledTasks(): Resource<List<VelaScheduledTask>> = safeApiCall {
        val response = apiService.listScheduledTasks()
        val domains = response.jobs?.map { 
            VelaScheduledTask(
                id = it.id ?: "",
                command = it.command ?: "",
                nextRun = it.nextRun ?: it.runAt ?: "Unknown",
                recurring = it.recurring
            )
        } ?: emptyList()
        velaDao.replaceScheduledTasks(domains.map { VelaScheduledTaskEntity.fromDomain(it) })
        domains
    }

    override suspend fun createScheduledTask(
        command: String,
        runAt: String,
        recurring: String?
    ): Resource<VelaScheduledTask> = safeApiCall {
        val res = apiService.createScheduledTask(SchedulerCreateRequest(command, runAt, recurring))
        val domain = VelaScheduledTask(
            id = res.id ?: "",
            command = res.command ?: command,
            nextRun = res.nextRun ?: runAt,
            recurring = res.recurring ?: recurring
        )
        velaDao.upsertScheduledTasks(listOf(VelaScheduledTaskEntity.fromDomain(domain)))
        domain
    }

    override suspend fun cancelScheduledTask(taskId: String): Resource<Unit> = safeApiCall {
        apiService.cancelScheduledTask(taskId)
        velaDao.deleteScheduledTask(taskId)
        Unit
    }

    override suspend fun runTaskNow(taskId: String): Resource<Unit> = safeApiCall {
        apiService.runTaskNow(taskId)
        Unit
    }

    // --- Maintenance ---

    override suspend fun clearCache(): Resource<Unit> = safeApiCall {
        apiService.clearCache()
        Unit
    }

    override suspend fun getLogs(service: String, lines: Int): Resource<VelaLogs> = safeApiCall {
        val res = apiService.getLogs(service, lines)
        VelaLogs(
            service = res.service ?: service,
            lines = res.lines ?: emptyList()
        )
    }

    override suspend fun checkUpdates(): Resource<VelaMaintenanceUpdate> = safeApiCall {
        val res = apiService.checkUpdates()
        VelaMaintenanceUpdate(
            updatesAvailable = res.updatesAvailable ?: false,
            packages = res.packages?.map { VelaPackageUpdate(it.name ?: "Unknown", it.version ?: "Unknown") } ?: emptyList()
        )
    }

    override suspend fun runUpdates(): Resource<Unit> = safeApiCall {
        apiService.runUpdates()
        Unit
    }

    override suspend fun syncTime(): Resource<Unit> = safeApiCall {
        apiService.syncTime()
        Unit
    }

    override suspend fun getServices(): Resource<List<VelaService>> = safeApiCall {
        val res = apiService.getServices()
        res.services?.map { VelaService(it.name ?: "Unknown", it.active ?: false) } ?: emptyList()
    }

    override suspend fun startService(name: String): Resource<Unit> = safeApiCall {
        apiService.startService(ServiceActionRequest(name))
        Unit
    }

    override suspend fun stopService(name: String): Resource<Unit> = safeApiCall {
        apiService.stopService(ServiceActionRequest(name))
        Unit
    }

    override suspend fun restartService(name: String): Resource<Unit> = safeApiCall {
        apiService.restartService(ServiceActionRequest(name))
        Unit
    }

    private fun parseProcessesResiliently(jsonStr: String): List<VelaProcess> {
        try {
            val listType = Types.newParameterizedType(List::class.java, ProcessItem::class.java)
            val adapter = moshi.adapter<List<ProcessItem>>(listType)
            val list = adapter.fromJson(jsonStr)
            if (list != null) return list.map { it.toDomain() }
        } catch (e: Exception) {}

        try {
            val adapter = moshi.adapter(ProcessesResponse::class.java)
            val obj = adapter.fromJson(jsonStr)
            if (obj?.topByCpu != null) return obj.topByCpu.map { it.toDomain() }
        } catch (e: Exception) {}

        try {
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Any>>(mapType)
            val parsedMap = adapter.fromJson(jsonStr)
            if (parsedMap != null) {
                for ((_, value) in parsedMap) {
                    if (value is List<*>) {
                        val subJson = moshi.adapter(Any::class.java).toJson(value)
                        val listType = Types.newParameterizedType(List::class.java, ProcessItem::class.java)
                        val list = moshi.adapter<List<ProcessItem>>(listType).fromJson(subJson)
                        if (list != null) return list.map { it.toDomain() }
                    }
                }
            }
        } catch (e: Exception) {}

        return emptyList()
    }

    private fun ProcessItem.toDomain() = VelaProcess(
        pid = pid ?: 0,
        name = name ?: "Unknown",
        cpu = cpu ?: 0.0,
        mem = mem ?: 0.0,
        username = username,
        memoryRss = memRss
    )

    private fun FileItem.toDomain() = VelaFileInfo(
        name = name ?: "",
        path = normalizePath(path ?: ""),
        type = type ?: "file",
        size = size ?: 0L,
        modified = modified ?: 0.0,
        isHidden = isHidden ?: false,
        hasChildren = hasChildren ?: false,
        childrenCount = childrenCount,
        extension = extension
    )

    private fun normalizePath(path: String): String {
        if (path.isEmpty()) return ""
        if (path == "/") return "/"
        return path.removeSuffix("/")
    }
}
