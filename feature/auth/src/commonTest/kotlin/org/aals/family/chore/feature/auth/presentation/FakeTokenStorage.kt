package org.aals.family.chore.feature.auth.presentation

import org.aals.family.chore.core.domain.repository.TokenStorage

class FakeTokenStorage : TokenStorage {
    private var token: String? = null
    private var familyId: String? = null
    private var serverUrl: String? = null

    override suspend fun saveToken(token: String) {
        this.token = token
    }

    override suspend fun getToken(): String? = token

    override suspend fun saveFamilyId(familyId: String) {
        this.familyId = familyId
    }

    override suspend fun getFamilyId(): String? = familyId

    override suspend fun saveServerUrl(url: String) {
        this.serverUrl = url
    }

    override suspend fun getServerUrl(): String? = serverUrl

    override suspend fun clear() {
        token = null
        familyId = null
        serverUrl = null
    }
}
