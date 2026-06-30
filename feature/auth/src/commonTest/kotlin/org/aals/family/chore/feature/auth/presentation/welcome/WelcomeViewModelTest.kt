package org.aals.family.chore.feature.auth.presentation.welcome

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.aals.family.chore.feature.auth.presentation.FakeTokenStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var tokenStorage: FakeTokenStorage
    private lateinit var viewModel: WelcomeViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tokenStorage = FakeTokenStorage()
    }

    private fun createViewModel() {
        viewModel = WelcomeViewModel(tokenStorage)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads server name from token storage`() = runTest {
        tokenStorage.saveServerName("Test Family")
        createViewModel()

        viewModel.state.test {
            assertThat(awaitItem().serverName).isEqualTo("Test Family")
        }
    }

    @Test
    fun `clicking setup new family sends NavigateToSetupFamily event`() = runTest {
        createViewModel()
        viewModel.events.test {
            viewModel.onAction(WelcomeAction.OnSetupNewFamilyClick)
            assertThat(awaitItem()).isEqualTo(WelcomeEvent.NavigateToSetupFamily)
        }
    }

    @Test
    fun `clicking join family sends NavigateToJoinFamily event`() = runTest {
        createViewModel()
        viewModel.events.test {
            viewModel.onAction(WelcomeAction.OnJoinFamilyClick)
            assertThat(awaitItem()).isEqualTo(WelcomeEvent.NavigateToJoinFamily)
        }
    }
}
