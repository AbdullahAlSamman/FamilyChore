package org.aals.family.chore.core.domain.repository

interface TokenStorage {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun saveFamilyId(familyId: String)
    suspend fun getFamilyId(): String?
    suspend fun saveServerUrl(url: String)
    suspend fun getServerUrl(): String?
    suspend fun saveServerName(name: String)
    suspend fun getServerName(): String?
    suspend fun clear()
}
