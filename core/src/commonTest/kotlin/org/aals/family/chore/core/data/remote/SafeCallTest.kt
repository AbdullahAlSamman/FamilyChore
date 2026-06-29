package org.aals.family.chore.core.data.remote

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import kotlin.test.Test

class SafeCallTest {

    @Test
    fun `responseToResult maps 401 to UNAUTHORIZED`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", status = HttpStatusCode.Unauthorized)
        })
        val response = client.get("/test")
        val result = responseToResult<Unit>(response)
        
        assertThat(result).isEqualTo(Result.Error(DataError.Network.UNAUTHORIZED))
    }

    @Test
    fun `responseToResult maps 408 to REQUEST_TIMEOUT`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", status = HttpStatusCode.RequestTimeout)
        })
        val response = client.get("/test")
        val result = responseToResult<Unit>(response)
        
        assertThat(result).isEqualTo(Result.Error(DataError.Network.REQUEST_TIMEOUT))
    }

    @Test
    fun `responseToResult maps 409 to CONFLICT`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", status = HttpStatusCode.Conflict)
        })
        val response = client.get("/test")
        val result = responseToResult<Unit>(response)
        
        assertThat(result).isEqualTo(Result.Error(DataError.Network.CONFLICT))
    }

    @Test
    fun `responseToResult maps 413 to PAYLOAD_TOO_LARGE`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", status = HttpStatusCode.PayloadTooLarge)
        })
        val response = client.get("/test")
        val result = responseToResult<Unit>(response)
        
        assertThat(result).isEqualTo(Result.Error(DataError.Network.PAYLOAD_TOO_LARGE))
    }

    @Test
    fun `responseToResult maps 429 to TOO_MANY_REQUESTS`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", status = HttpStatusCode.TooManyRequests)
        })
        val response = client.get("/test")
        val result = responseToResult<Unit>(response)
        
        assertThat(result).isEqualTo(Result.Error(DataError.Network.TOO_MANY_REQUESTS))
    }

    @Test
    fun `responseToResult maps 500 to SERVER_ERROR`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", status = HttpStatusCode.InternalServerError)
        })
        val response = client.get("/test")
        val result = responseToResult<Unit>(response)
        
        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERVER_ERROR))
    }

    @Test
    fun `responseToResult maps 2xx to Success`() = runTest {
        val client = HttpClient(MockEngine {
            respond("success", status = HttpStatusCode.OK)
        })
        val response = client.get("/test")
        val result = responseToResult<String>(response)
        
        assertThat(result).isEqualTo(Result.Success("success"))
    }

    @Test
    fun `safeCall returns NO_INTERNET on UnresolvedAddressException`() = runTest {
        val client = HttpClient(MockEngine {
            throw io.ktor.util.network.UnresolvedAddressException()
        })
        
        val result = client.get<Unit>("/test")
        
        assertThat(result).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
    }

    @Test
    fun `safeCall returns SERIALIZATION on SerializationException`() = runTest {
        val client = HttpClient(MockEngine {
            throw kotlinx.serialization.SerializationException()
        })
        
        val result = client.get<Unit>("/test")
        
        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERIALIZATION))
    }
}
