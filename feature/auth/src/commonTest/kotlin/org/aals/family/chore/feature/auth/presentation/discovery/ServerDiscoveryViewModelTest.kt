package org.aals.family.chore.feature.auth.presentation.discovery

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.aals.family.chore.core.domain.discovery.DiscoveredServer
import org.aals.family.chore.feature.auth.presentation.FakeTokenStorage
import org.aals.family.chore.feature.auth.presentation.welcome.FakeServerDiscovery
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServerDiscoveryViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ServerDiscoveryViewModel
    private lateinit var serverDiscovery: FakeServerDiscovery
    private lateinit var tokenStorage: FakeTokenStorage

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        serverDiscovery = FakeServerDiscovery()
        tokenStorage = FakeTokenStorage()
        viewModel = ServerDiscoveryViewModel(serverDiscovery, tokenStorage, SavedStateHandle())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `discovered servers update state`() = runTest {
        val server1 = DiscoveredServer("Server 1", "http://1.1.1.1:8080")
        val server2 = DiscoveredServer("Server 2", "http://2.2.2.2:8080")

        viewModel.state.test {
            assertThat(awaitItem().discoveredServers).isEqualTo(emptyList())
            serverDiscovery.emit(server1)
            assertThat(awaitItem().discoveredServers).contains(server1)
            serverDiscovery.emit(server2)
            assertThat(awaitItem().discoveredServers).contains(server2)
        }
    }

    @Test
    fun `selecting server saves to storage and navigates`() = runTest {
        val server = DiscoveredServer("My Server", "http://my.server:8080")

        viewModel.events.test {
            viewModel.onAction(ServerDiscoveryAction.OnServerSelected(server))

            assertThat(tokenStorage.getServerUrl()).isEqualTo(server.url)
            assertThat(tokenStorage.getServerName()).isEqualTo(server.name)
            assertThat(awaitItem()).isEqualTo(ServerDiscoveryEvent.NavigateToWelcome)
        }
    }

    @Test
    fun `clicking scan again clears results and restarts`() = runTest {
        val server = DiscoveredServer("My Server", "http://my.server:8080")
        serverDiscovery.emit(server)

        assertThat(viewModel.state.value.discoveredServers).contains(server)

        viewModel.onAction(ServerDiscoveryAction.OnScanAgainClick)

        assertThat(viewModel.state.value.discoveredServers).isEqualTo(emptyList())
        assertThat(viewModel.state.value.isScanning).isEqualTo(true)
    }
}
