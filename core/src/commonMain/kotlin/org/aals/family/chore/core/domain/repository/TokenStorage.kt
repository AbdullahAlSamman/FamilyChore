package org.aals.family.chore.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface TokenStorage {
    val token: Flow<String?>
    val familyId: Flow<String?>
    val userId: Flow<String?>
    val isOfflineMode: Flow<Boolean?>
    val serverUrl: Flow<String?>
    val serverName: Flow<String?>
    val language: Flow<String?>

    suspend fun setOfflineMode(enabled: Boolean?)
    suspend fun getOfflineMode(): Boolean?
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun saveFamilyId(familyId: String)
    suspend fun getFamilyId(): String?
    suspend fun saveUserId(userId: String)
    suspend fun getUserId(): String?
    suspend fun saveServerUrl(url: String)
    suspend fun getServerUrl(): String?
    suspend fun saveServerName(name: String)
    suspend fun getServerName(): String?
    suspend fun saveLanguage(languageCode: String)
    suspend fun getLanguage(): String?
    suspend fun clear()
    suspend fun clearAuth()
}
