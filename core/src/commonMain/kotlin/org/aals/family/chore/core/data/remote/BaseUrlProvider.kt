package org.aals.family.chore.core.data.remote

import org.aals.family.chore.core.domain.repository.TokenStorage

interface BaseUrlProvider {
    suspend fun getBaseUrl(): String?
}

class TokenStorageBaseUrlProvider(
    private val tokenStorage: TokenStorage
) : BaseUrlProvider {
    override suspend fun getBaseUrl(): String? {
        return tokenStorage.getServerUrl()
    }
}
