package org.aals.family.chore.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.aals.family.chore.core.domain.repository.TokenStorage

class FakeTokenStorage : TokenStorage {
    private val _token = MutableStateFlow<String?>(null)
    private val _familyId = MutableStateFlow<String?>(null)
    private val _userId = MutableStateFlow<String?>(null)
    private val _serverUrl = MutableStateFlow<String?>(null)
    private val _serverName = MutableStateFlow<String?>(null)
    private val _language = MutableStateFlow<String?>(null)

    override val token: Flow<String?> = _token
    override val familyId: Flow<String?> = _familyId
    override val userId: Flow<String?> = _userId
    override val serverUrl: Flow<String?> = _serverUrl
    override val serverName: Flow<String?> = _serverName
    override val language: Flow<String?> = _language

    override suspend fun saveToken(token: String) {
        _token.value = token
    }

    override suspend fun getToken(): String? = _token.value

    override suspend fun saveFamilyId(familyId: String) {
        _familyId.value = familyId
    }

    override suspend fun getFamilyId(): String? = _familyId.value

    override suspend fun saveUserId(userId: String) {
        _userId.value = userId
    }

    override suspend fun getUserId(): String? = _userId.value

    override suspend fun saveServerUrl(url: String) {
        _serverUrl.value = url
    }

    override suspend fun getServerUrl(): String? = _serverUrl.value

    override suspend fun saveServerName(name: String) {
        _serverName.value = name
    }

    override suspend fun getServerName(): String? = _serverName.value

    override suspend fun saveLanguage(languageCode: String) {
        _language.value = languageCode
    }

    override suspend fun getLanguage(): String? = _language.value

    override suspend fun clear() {
        _token.value = null
        _familyId.value = null
        _userId.value = null
        _serverUrl.value = null
        _serverName.value = null
        _language.value = null
    }

    override suspend fun clearAuth() {
        _token.value = null
        _userId.value = null
    }
}
