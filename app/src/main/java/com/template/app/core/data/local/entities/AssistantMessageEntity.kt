package com.template.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.template.app.domain.model.AssistantChatMessage
import com.template.app.domain.model.AssistantConfirmation

@Entity(tableName = "assistant_messages")
data class AssistantMessageEntity(
    @PrimaryKey val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val imageBase64: String?,
    val artUrl: String?,
    val isPinRequired: Boolean,
    val pendingActionId: String?,
    // Confirmation fields (flattened for simplicity or could be serialized)
    val confTitle: String?,
    val confDescription: String?,
    val confActionType: String?,
    val confDetails: List<String>?,
    val confPromptText: String?,
    val confExpiresInSeconds: Int?,
    val confRequiresAuth: Boolean?
) {
    fun toDomain(): AssistantChatMessage {
        val confirmation = if (confTitle != null) {
            AssistantConfirmation(
                title = confTitle,
                description = confDescription ?: "",
                actionType = confActionType ?: "",
                details = confDetails ?: emptyList(),
                promptText = confPromptText ?: "",
                expiresInSeconds = confExpiresInSeconds,
                requiresAuth = confRequiresAuth ?: false
            )
        } else null

        return AssistantChatMessage(
            id = id,
            text = text,
            isUser = isUser,
            timestamp = timestamp,
            imageBase64 = imageBase64,
            artUrl = artUrl,
            confirmation = confirmation,
            isPinRequired = isPinRequired,
            pendingActionId = pendingActionId
        )
    }

    companion object {
        fun fromDomain(domain: AssistantChatMessage) = AssistantMessageEntity(
            id = domain.id,
            text = domain.text,
            isUser = domain.isUser,
            timestamp = domain.timestamp,
            imageBase64 = domain.imageBase64,
            artUrl = domain.artUrl,
            isPinRequired = domain.isPinRequired,
            pendingActionId = domain.pendingActionId,
            confTitle = domain.confirmation?.title,
            confDescription = domain.confirmation?.description,
            confActionType = domain.confirmation?.actionType,
            confDetails = domain.confirmation?.details,
            confPromptText = domain.confirmation?.promptText,
            confExpiresInSeconds = domain.confirmation?.expiresInSeconds,
            confRequiresAuth = domain.confirmation?.requiresAuth
        )
    }
}
