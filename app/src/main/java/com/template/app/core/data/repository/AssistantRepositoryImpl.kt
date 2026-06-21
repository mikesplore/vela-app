package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.AssistantDao
import com.template.app.core.data.local.entities.AssistantMessageEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.AssistantRequest
import com.template.app.core.data.remote.dto.AssistantResponse
import com.template.app.core.data.remote.dto.ConfirmationCard
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.AssistantChatMessage
import com.template.app.domain.model.AssistantConfirmation
import com.template.app.domain.repository.AssistantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val assistantDao: AssistantDao
) : AssistantRepository {

    private val sessionId = UUID.randomUUID().toString()

    override fun observeMessages(): Flow<List<AssistantChatMessage>> = 
        assistantDao.observeMessages().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun sendMessage(message: String): Resource<Unit> = safeApiCall {
        // Add user message to local cache
        val userMsg = AssistantChatMessage(text = message, isUser = true)
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(userMsg))

        val response = apiService.assistantChat(
            sessionId = sessionId,
            body = AssistantRequest(message = message)
        )

        val assistantMsg = response.toDomain()
        // Add assistant response to local cache
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(assistantMsg))
        Unit
    }

    override suspend fun clearChat() {
        assistantDao.clearChat()
    }

    private fun AssistantResponse.toDomain(): AssistantChatMessage {
        return AssistantChatMessage(
            text = reply,
            isUser = false,
            imageBase64 = imageBase64,
            artUrl = artUrl,
            confirmation = confirmation?.toDomain(expiresInSeconds),
            isPinRequired = requiresAuth,
            pendingActionId = pendingActionId
        )
    }

    private fun ConfirmationCard.toDomain(expiresInSeconds: Int?): AssistantConfirmation {
        return AssistantConfirmation(
            title = title,
            description = description,
            actionType = actionType,
            details = actionDetails,
            promptText = promptText,
            expiresInSeconds = expiresInSeconds,
            requiresAuth = requiresAuth
        )
    }
}
