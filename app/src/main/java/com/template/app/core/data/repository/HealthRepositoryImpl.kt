package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaDeviceEntity
import com.template.app.core.data.local.entities.VelaHealthEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaDevice
import com.template.app.domain.model.VelaHealth
import com.template.app.domain.repository.HealthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class HealthRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao
) : HealthRepository {

    private val remoteHealth = MutableStateFlow<VelaHealth?>(null)
    private val remoteDevice = MutableStateFlow<VelaDevice?>(null)

    override fun observeHealth(): Flow<VelaHealth?> =
        velaDao.observeHealth()
            .map { it?.toDomain() }
            .combine(remoteHealth) { local, remote ->
                remote ?: local
            }
            .distinctUntilChanged()

    override suspend fun getHealth(): Resource<VelaHealth> = safeApiCall {
        try {
            val response = apiService.health()
            val domain = VelaHealth(
                status = response.status ?: "unknown",
                uptimeSeconds = response.uptimeSeconds ?: 0L
            )
            remoteHealth.value = domain
            velaDao.upsertHealth(VelaHealthEntity.fromDomain(domain))
            domain
        } catch (e: Exception) {
            remoteHealth.value = null
            velaDao.clearHealth()
            throw e
        }
    }

    override fun observeDevice(): Flow<VelaDevice?> =
        velaDao.observeDevice()
            .map { it?.toDomain() }
            .combine(remoteDevice) { local, remote ->
                remote ?: local
            }
            .distinctUntilChanged()

    override suspend fun getDevice(): Resource<VelaDevice> = safeApiCall {
        val response = apiService.getDevice()
        val domain = VelaDevice(
            laptopModel = response.laptopModel,
            hardwareVendor = response.hardwareVendor,
            osDistro = response.osDistro,
            osDistroVersion = response.osDistroVersion,
            kernel = response.kernel,
            architecture = response.architecture,
            hostname = response.hostname,
            prettyHostname = response.prettyHostname
        )
        remoteDevice.value = domain
        velaDao.upsertDevice(VelaDeviceEntity.fromDomain(domain))
        domain
    }
}
