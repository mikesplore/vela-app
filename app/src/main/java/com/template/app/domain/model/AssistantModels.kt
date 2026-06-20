package com.template.app.domain.model

import java.util.UUID

data class AssistantChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null,
    val artUrl: String? = null,
    val confirmation: AssistantConfirmation? = null,
    val isPinRequired: Boolean = false,
    val pendingActionId: String? = null
)

data class AssistantConfirmation(
    val title: String,
    val description: String,
    val actionType: String,
    val details: List<String>,
    val promptText: String,
    val expiresInSeconds: Int? = null,
    val requiresAuth: Boolean = false
)
