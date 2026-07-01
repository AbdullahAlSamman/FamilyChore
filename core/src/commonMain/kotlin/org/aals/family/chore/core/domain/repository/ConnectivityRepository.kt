package org.aals.family.chore.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

interface ConnectivityRepository {
    val isServerReachable: Flow<Boolean>
    suspend fun checkHealth(): Result<Unit, DataError.Network>
}
