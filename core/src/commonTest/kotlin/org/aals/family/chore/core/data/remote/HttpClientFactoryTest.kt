package org.aals.family.chore.core.data.remote

import co.touchlab.kermit.Logger
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpClientFactoryTest {

    @Test
    fun `merges base URL and relative path correctly`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("localhost", request.url.host)
            assertEquals(8080, request.url.port)
            respond("OK")
        }

        val baseUrlProvider = object : BaseUrlProvider {
            override suspend fun getBaseUrl(): String = "http://localhost:8080/api/"
        }

        val client = HttpClientFactory.create(engine, baseUrlProvider, Logger.withTag("Test"))

        client.get("auth/login")
    }

    @Test
    fun `does not overwrite absolute URL`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("https://other-server.com/test", request.url.toString())
            respond("OK")
        }

        val baseUrlProvider = object : BaseUrlProvider {
            override suspend fun getBaseUrl(): String = "http://localhost:8080/api/"
        }

        val client = HttpClientFactory.create(engine, baseUrlProvider, Logger.withTag("Test"))

        client.get("https://other-server.com/test")
    }
}
