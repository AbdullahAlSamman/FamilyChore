package org.aals.family.chore.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aals.family.chore.core.data.remote.ServerHealthDataSource
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.onSuccess
import org.aals.family.chore.core.domain.util.onFailure
import kotlin.time.Duration.Companion.milliseconds

class ConnectivityRepositoryImpl(
    private val healthDataSource: ServerHealthDataSource,
    private val tokenStorage: TokenStorage,
    private val applicationScope: CoroutineScope
) : ConnectivityRepository {

    private val _isServerReachable = MutableStateFlow(true)
    override val isServerReachable: Flow<Boolean> = _isServerReachable.asStateFlow()

    init {
        startPolling()
    }

    override suspend fun checkHealth(): Result<Unit, DataError.Network> {
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        return healthDataSource.checkHealth(serverUrl)
            .onSuccess { _isServerReachable.value = true }
            .onFailure { _isServerReachable.value = false }
    }

    private fun startPolling() {
        applicationScope.launch {
            while (true) {
                val serverUrl = tokenStorage.getServerUrl()
                if (serverUrl != null) {
                    checkHealth()
                }
                delay(10000.milliseconds) // Poll every 10 seconds
            }
        }
    }
}
