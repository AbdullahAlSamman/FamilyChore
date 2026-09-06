package org.aals.family.chore.feature.auth.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

class FakeConnectivityRepository : ConnectivityRepository {
    val isServerReachableFlow = MutableStateFlow(true)
    var healthResult: Result<Unit, DataError.Network> = Result.Success(Unit)

    override val isServerReachable: Flow<Boolean> = isServerReachableFlow

    override suspend fun checkHealth(): Result<Unit, DataError.Network> = healthResult
}
