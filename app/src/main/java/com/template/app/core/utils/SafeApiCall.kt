package com.template.app.core.utils

import com.template.app.core.network.NetworkErrors
import retrofit2.HttpException
import java.io.IOException

/**
 * Wraps a suspend Retrofit/Room call in a try-catch and maps the result
 * to a [Resource]. Use this in every Repository to avoid boilerplate.
 */
suspend fun <T> safeApiCall(call: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(call())
    } catch (e: HttpException) {
        Resource.Error(NetworkErrors.getMessageForCode(e.code()), e)
    } catch (e: IOException) {
        Resource.Error(NetworkErrors.NETWORK_ERROR, e)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: NetworkErrors.GENERIC_ERROR, e)
    }
}
