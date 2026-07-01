package com.template.app.core.network

object NetworkErrors {
    fun getMessageForCode(code: Int): String = when (code) {
        400 -> "Bad request"
        401 -> "Unauthorized — please log in again"
        403 -> "Please check your credentials"
        404 -> "Resource not found"
        408 -> "Request timed out"
        500 -> "Server error, please try again later"
        else -> "Error $code"
    }

    const val GENERIC_ERROR = "An unexpected error occurred"
    const val NETWORK_ERROR = "Network error — check your connection"
}
