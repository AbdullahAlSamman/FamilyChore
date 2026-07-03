package org.aals.family.chore.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.map

open class ServerHealthDataSource(
    private val httpClient: HttpClient
) {
    open suspend fun checkHealth(serverUrl: String? = null): Result<Unit, DataError.Network> {
        return safeCall<String> {
            httpClient.get {
                if (serverUrl != null) {
                    url(serverUrl)
                } else {
                    url("")
                }
            }
        }.map { Unit }
    }
}
