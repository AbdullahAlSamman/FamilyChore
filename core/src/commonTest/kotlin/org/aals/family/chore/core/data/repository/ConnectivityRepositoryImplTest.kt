package org.aals.family.chore.core.data.repository

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.aals.family.chore.core.data.remote.ServerHealthDataSource
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityRepositoryImplTest {

    private lateinit var repository: ConnectivityRepositoryImpl
    private lateinit var healthDataSource: FakeServerHealthDataSource
    private lateinit var tokenStorage: FakeTokenStorage
    private val testScope = TestScope()

    @BeforeTest
    fun setUp() {
        healthDataSource = FakeServerHealthDataSource()
        tokenStorage = FakeTokenStorage()
        repository = ConnectivityRepositoryImpl(healthDataSource, tokenStorage, testScope)
    }

    @Test
    fun `isServerReachable updates on checkHealth`() = runTest {
        tokenStorage.saveServerUrl("http://localhost:8080")
        
        repository.isServerReachable.test {
            assertThat(awaitItem()).isEqualTo(true) // Initial value

            healthDataSource.result = Result.Error(DataError.Network.SERVER_ERROR)
            repository.checkHealth()
            assertThat(awaitItem()).isEqualTo(false)

            healthDataSource.result = Result.Success(Unit)
            repository.checkHealth()
            assertThat(awaitItem()).isEqualTo(true)
        }
    }
}

class FakeServerHealthDataSource : ServerHealthDataSource {
    var result: Result<Unit, DataError.Network> = Result.Success(Unit)
    override suspend fun checkHealth(serverUrl: String?): Result<Unit, DataError.Network> = result
}
