package org.aals.family.chore.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.aals.family.chore.core.domain.repository.TokenStorage
import co.touchlab.kermit.Logger as KermitLogger

object HttpClientFactory {
    fun create(
        engine: HttpClientEngine,
        baseUrlProvider: BaseUrlProvider,
        tokenStorage: TokenStorage,
        kermitLogger: KermitLogger
    ): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        kermitLogger.d { message }
                    }
                }
                level = LogLevel.ALL
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        tokenStorage.getToken()?.let { BearerTokens(it, "") }
                    }
                    refreshTokens {
                        // TODO: Implement refresh logic
                        null
                    }
                }
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                
                runBlocking {
                    baseUrlProvider.getBaseUrl()?.let { baseUrlString ->
                        if (url.host.isEmpty()) {
                            // Prepend base URL for relative requests
                            val originalPath = url.pathSegments.filter { it.isNotEmpty() }
                            url.takeFrom(baseUrlString)
                            if (originalPath.isNotEmpty()) {
                                url.pathSegments = url.pathSegments.filter { it.isNotEmpty() } + originalPath
                            }
                        }
                    }
                }
            }
        }
    }
}
