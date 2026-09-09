package org.aals.family.chore.core.domain.usecase

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.aals.family.chore.core.data.repository.FakeTokenStorage
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import kotlin.test.BeforeTest
import kotlin.test.Test

class ObserveConnectivityUseCaseTest {

    private lateinit var useCase: ObserveConnectivityUseCase
    private lateinit var tokenStorage: FakeTokenStorage
    private lateinit var connectivityRepository: FakeConnectivityRepository

    @BeforeTest
    fun setUp() {
        tokenStorage = FakeTokenStorage()
        connectivityRepository = FakeConnectivityRepository()
        useCase = ObserveConnectivityUseCase(tokenStorage, connectivityRepository)
    }

    @Test
    fun `emits combined connectivity status when offline mode or reachability changes`() = runTest {
        useCase().test {
            assertThat(awaitItem()).isEqualTo(ConnectivityStatus(isOfflineMode = false, isServerReachable = true))

            tokenStorage.setOfflineMode(true)
            assertThat(awaitItem()).isEqualTo(ConnectivityStatus(isOfflineMode = true, isServerReachable = true))

            connectivityRepository.isServerReachableFlow.value = false
            assertThat(awaitItem()).isEqualTo(ConnectivityStatus(isOfflineMode = true, isServerReachable = false))
        }
    }
}

private class FakeConnectivityRepository : ConnectivityRepository {
    val isServerReachableFlow = MutableStateFlow(true)
    override val isServerReachable = isServerReachableFlow
    override suspend fun checkHealth(): Result<Unit, DataError.Network> = Result.Success(Unit)
}
