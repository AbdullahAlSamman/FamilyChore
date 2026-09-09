package org.aals.family.chore.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TokenStorage

data class ConnectivityStatus(
    val isOfflineMode: Boolean = false,
    val isServerReachable: Boolean = true,
)

class ObserveConnectivityUseCase(
    private val tokenStorage: TokenStorage,
    private val connectivityRepository: ConnectivityRepository,
) {
    operator fun invoke(): Flow<ConnectivityStatus> {
        return combine(
            tokenStorage.isOfflineMode,
            connectivityRepository.isServerReachable,
        ) { isOffline, isReachable ->
            ConnectivityStatus(
                isOfflineMode = isOffline ?: false,
                isServerReachable = isReachable,
            )
        }
    }
}
