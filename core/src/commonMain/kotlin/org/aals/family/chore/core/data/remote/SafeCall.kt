package org.aals.family.chore.core.data.remote

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

suspend inline fun <reified Response : Any> HttpClient.get(
    route: String,
    queryParameters: Map<String, Any?> = mapOf()
): Result<Response, DataError.Network> {
    return safeCall {
        get {
            url(route)
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.post(
    route: String,
    body: Request
): Result<Response, DataError.Network> {
    return safeCall {
        post {
            url(route)
            setBody(body)
        }
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.patch(
    route: String,
    body: Request
): Result<Response, DataError.Network> {
    return safeCall {
        patch {
            url(route)
            setBody(body)
        }
    }
}

suspend inline fun <reified Response : Any> HttpClient.delete(
    route: String,
    queryParameters: Map<String, Any?> = mapOf()
): Result<Response, DataError.Network> {
    return safeCall {
        delete {
            url(route)
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Network> {
    return try {
        val response = execute()
        responseToResult(response)
    } catch (e: UnresolvedAddressException) {
        Logger.e(e) { "Unresolved Address Exception" }
        Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: ConnectTimeoutException) {
        Logger.e(e) { "Connect Timeout Exception" }
        Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: HttpRequestTimeoutException) {
        Logger.e(e) { "HTTP Request Timeout Exception" }
        Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: SocketTimeoutException) {
        Logger.e(e) { "Socket Timeout Exception" }
        Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: IOException) {
        Logger.e(e) { "IO Exception / Network unreachable" }
        Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SerializationException) {
        Logger.e(e) { "Serialization Exception" }
        Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Logger.e(e) { "Network Error: ${e::class.simpleName} - ${e.message}" }
        val className = e::class.simpleName ?: ""
        val message = e.message ?: ""
        if (className.contains("Connect") || className.contains("Host") || className.contains("Socket") || className.contains("Network") || message.contains("Failed to connect", ignoreCase = true) || message.contains("Connection refused", ignoreCase = true)) {
            Result.Error(DataError.Network.NO_INTERNET)
        } else {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> Result.Success(response.body<T>())
        400 -> Result.Error(DataError.Network.VALIDATION_ERROR)
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        403 -> Result.Error(DataError.Network.FORBIDDEN)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Network.CONFLICT)
        413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        503 -> Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}
