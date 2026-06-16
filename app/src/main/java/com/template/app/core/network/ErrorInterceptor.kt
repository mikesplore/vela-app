package com.template.app.core.network

import com.template.app.core.utils.AppEventManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * Intercepts network responses and emits snackbar events for errors.
 */
class ErrorInterceptor @Inject constructor(
    private val appEventManager: AppEventManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            appEventManager.showSnackbar("Network error — check your connection")
            throw e
        }

        if (!response.isSuccessful) {
            val errorMessage = when (response.code) {
                401 -> "Unauthorized — please log in again"
                403 -> "Forbidden"
                404 -> "Resource not found"
                500 -> "Server error, please try again later"
                else -> "Error ${response.code}: ${response.message}"
            }
            appEventManager.showSnackbar(errorMessage)
        }

        return response
    }
}
