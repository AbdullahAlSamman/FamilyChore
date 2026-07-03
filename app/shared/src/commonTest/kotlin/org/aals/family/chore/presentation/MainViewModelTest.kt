package org.aals.family.chore.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.feature.auth.presentation.navigation.ServerDiscoveryRoute
import org.aals.family.chore.feature.auth.presentation.navigation.UserSelectionRoute
import org.aals.family.chore.feature.auth.presentation.navigation.WelcomeRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.DashboardGraph
import kotlin.test.BeforeTest
import kotlin.test.Test

class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var tokenStorage: FakeTokenStorage
    private lateinit var connectivityRepository: FakeConnectivityRepository

    @BeforeTest
    fun setUp() {
        tokenStorage = FakeTokenStorage()
        connectivityRepository = FakeConnectivityRepository()
    }

    private fun createViewModel() {
        viewModel = MainViewModel(tokenStorage, connectivityRepository)
    }

    @Test
    fun `initial state is loading`() = runTest {
        createViewModel()
        assertThat(viewModel.state.value).isInstanceOf(MainState.Loading::class)
    }

    @Test
    fun `navigates to Discovery when no server URL`() = runTest {
        tokenStorage.clear()
        
        createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            val finalState = if (state is MainState.Loading) awaitItem() else state
            
            assertThat(finalState).isInstanceOf(MainState.Success::class)
            assertThat((finalState as MainState.Success).startDestination).isInstanceOf(ServerDiscoveryRoute::class)
        }
    }

    @Test
    fun `navigates to Dashboard when token exists`() = runTest {
        tokenStorage.saveToken("valid_token")
        
        createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            val finalState = if (state is MainState.Loading) awaitItem() else state
            
            assertThat(finalState).isInstanceOf(MainState.Success::class)
            assertThat((finalState as MainState.Success).startDestination).isEqualTo(DashboardGraph)
        }
    }

    @Test
    fun `navigates to UserSelection when server and family exist`() = runTest {
        tokenStorage.saveServerUrl("http://localhost")
        tokenStorage.saveFamilyId("family_123")
        connectivityRepository.healthResult = Result.Success(Unit)
        
        createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            val finalState = if (state is MainState.Loading) awaitItem() else state
            
            assertThat(finalState).isInstanceOf(MainState.Success::class)
            assertThat((finalState as MainState.Success).startDestination).isEqualTo(UserSelectionRoute(familyId = "family_123"))
        }
    }

    @Test
    fun `navigates to Welcome when server exists but no family`() = runTest {
        tokenStorage.saveServerUrl("http://localhost")
        connectivityRepository.healthResult = Result.Success(Unit)
        
        createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            val finalState = if (state is MainState.Loading) awaitItem() else state
            
            assertThat(finalState).isInstanceOf(MainState.Success::class)
            assertThat((finalState as MainState.Success).startDestination).isEqualTo(WelcomeRoute)
        }
    }

    @Test
    fun `navigates to Discovery Error when server unreachable`() = runTest {
        tokenStorage.saveServerUrl("http://localhost")
        connectivityRepository.healthResult = Result.Error(DataError.Network.SERVER_ERROR)
        
        createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            val finalState = if (state is MainState.Loading) awaitItem() else state
            
            assertThat(finalState).isInstanceOf(MainState.Success::class)
            assertThat((finalState as MainState.Success).startDestination).isEqualTo(ServerDiscoveryRoute(isErrorMode = true))
        }
    }

    private class FakeTokenStorage : TokenStorage {
        private val _token = MutableStateFlow<String?>(null)
        private val _familyId = MutableStateFlow<String?>(null)
        private val _serverUrl = MutableStateFlow<String?>(null)
        private val _serverName = MutableStateFlow<String?>(null)

        override val token: Flow<String?> = _token
        override val familyId: Flow<String?> = _familyId
        override val serverUrl: Flow<String?> = _serverUrl
        override val serverName: Flow<String?> = _serverName

        override suspend fun saveToken(token: String) { _token.value = token }
        override suspend fun getToken(): String? = _token.value
        override suspend fun saveFamilyId(familyId: String) { _familyId.value = familyId }
        override suspend fun getFamilyId(): String? = _familyId.value
        override suspend fun saveServerUrl(url: String) { _serverUrl.value = url }
        override suspend fun getServerUrl(): String? = _serverUrl.value
        override suspend fun saveServerName(name: String) { _serverName.value = name }
        override suspend fun getServerName(): String? = _serverName.value
        override suspend fun clear() {
            _token.value = null
            _familyId.value = null
            _serverUrl.value = null
            _serverName.value = null
        }
    }

    private class FakeConnectivityRepository : ConnectivityRepository {
        var healthResult: Result<Unit, DataError.Network> = Result.Success(Unit)
        override val isServerReachable: Flow<Boolean> = MutableStateFlow(true)
        override suspend fun checkHealth(): Result<Unit, DataError.Network> = healthResult
    }
}
