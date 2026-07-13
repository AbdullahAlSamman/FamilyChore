package org.aals.family.chore.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.aals.family.chore.core.domain.repository.TokenStorage

class DataStoreTokenStorage(
    private val dataStore: DataStore<Preferences>
) : TokenStorage {

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_FAMILY_ID = stringPreferencesKey("family_id")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_SERVER_NAME = stringPreferencesKey("server_name")
    }

    override val token: Flow<String?> = dataStore.data.map { it[KEY_TOKEN] }
    override val familyId: Flow<String?> = dataStore.data.map { it[KEY_FAMILY_ID] }
    override val userId: Flow<String?> = dataStore.data.map { it[KEY_USER_ID] }
    override val serverUrl: Flow<String?> = dataStore.data.map { it[KEY_SERVER_URL] }
    override val serverName: Flow<String?> = dataStore.data.map { it[KEY_SERVER_NAME] }

    override suspend fun saveToken(token: String) {
        dataStore.edit { it[KEY_TOKEN] = token }
    }

    override suspend fun getToken(): String? {
        return dataStore.data.map { it[KEY_TOKEN] }.first()
    }

    override suspend fun saveFamilyId(familyId: String) {
        dataStore.edit { it[KEY_FAMILY_ID] = familyId }
    }

    override suspend fun getFamilyId(): String? {
        return dataStore.data.map { it[KEY_FAMILY_ID] }.first()
    }

    override suspend fun saveUserId(userId: String) {
        dataStore.edit { it[KEY_USER_ID] = userId }
    }

    override suspend fun getUserId(): String? {
        return dataStore.data.map { it[KEY_USER_ID] }.first()
    }

    override suspend fun saveServerUrl(url: String) {
        dataStore.edit { it[KEY_SERVER_URL] = url }
    }

    override suspend fun getServerUrl(): String? {
        return dataStore.data.map { it[KEY_SERVER_URL] }.first()
    }

    override suspend fun saveServerName(name: String) {
        dataStore.edit { it[KEY_SERVER_NAME] = name }
    }

    override suspend fun getServerName(): String? {
        return dataStore.data.map { it[KEY_SERVER_NAME] }.first()
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    override suspend fun clearAuth() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
            preferences.remove(KEY_USER_ID)
        }
    }
}
