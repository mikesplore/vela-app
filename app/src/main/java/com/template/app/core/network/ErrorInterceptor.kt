package com.template.app.core.network

import com.squareup.moshi.Moshi
import com.template.app.core.data.remote.dto.ApiErrorResponse
import com.template.app.core.utils.AppEventManager
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import javax.inject.Inject

/**
 * Intercepts network responses and emits logging events for errors.
 * Global UI events (like 401 redirects) are handled here, while
 * user-facing messages are typically returned via Resource.Error in SafeApiCall.
 */
class ErrorInterceptor @Inject constructor(
    private val appEventManager: AppEventManager,
    private val moshi: Moshi
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            val errorMessage = NetworkErrors.NETWORK_ERROR
            // Log the error globally
            appEventManager.addNetworkErrorLog(
                url = request.url.toString(),
                method = request.method,
                code = 0,
                message = errorMessage
            )
            throw e
        }

        if (!response.isSuccessful) {
            val errorBody = response.body?.string()
            val apiError = errorBody?.let {
                try {
                    moshi.adapter(ApiErrorResponse::class.java).fromJson(it)
                } catch (e: Exception) {
                    null
                }
            }

            val errorMessage = apiError?.message ?: NetworkErrors.getMessageForCode(response.code)
            
            // Add to network logs for debugging
            appEventManager.addNetworkErrorLog(
                url = request.url.toString(),
                method = request.method,
                code = response.code,
                message = errorMessage
            )

            // Handle global session expiry
            if (response.code == 401) {
                appEventManager.showNetworkErrorSnackbar("Session expired. Please log in again.")
            }

            // Re-create the response body since we've consumed it
            return response.newBuilder()
                .body((errorBody ?: "").toResponseBody(response.body?.contentType()))
                .build()
        }

        return response
    }
}
