package com.template.app.core.data.repository

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService
) : AssistantRepository {

    private val sessionId = UUID.randomUUID().toString()
    private val _messages = MutableStateFlow<List<AssistantChatMessage>>(emptyList())

    override fun observeMessages(): Flow<List<AssistantChatMessage>> = _messages.asStateFlow()

    override suspend fun sendMessage(message: String): Resource<Unit> = safeApiCall {
        // Add user message to list
        val userMsg = AssistantChatMessage(text = message, isUser = true)
        _messages.update { it + userMsg }

        val response = apiService.assistantChat(
            sessionId = sessionId,
            body = AssistantRequest(message = message)
        )

        val assistantMsg = response.toDomain()
        _messages.update { it + assistantMsg }
        Unit
    }

    override suspend fun clearChat() {
        _messages.value = emptyList()
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
