package org.aals.family.chore.core.data.remote

import assertk.assertThat
import assertk.assertions.isInstanceOf
import co.touchlab.kermit.Logger
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.aals.family.chore.core.domain.util.Result
import kotlin.test.Test

class ServerHealthDataSourceTest {

    @Test
    fun `checkHealth returns success when server responds 200`() = runTest {
        val engine = MockEngine {
            respond("OK", HttpStatusCode.OK)
        }
        val httpClient = HttpClientFactory.create(engine, Logger.withTag("Test"))
        val dataSource = ServerHealthDataSource(httpClient)

        val result = dataSource.checkHealth("http://localhost:8080")

        assertThat(result).isInstanceOf(Result.Success::class)
    }

    @Test
    fun `checkHealth returns error when server responds 500`() = runTest {
        val engine = MockEngine {
            respond("Error", HttpStatusCode.InternalServerError)
        }
        val httpClient = HttpClientFactory.create(engine, Logger.withTag("Test"))
        val dataSource = ServerHealthDataSource(httpClient)

        val result = dataSource.checkHealth("http://localhost:8080")

        assertThat(result).isInstanceOf(Result.Error::class)
    }
}
