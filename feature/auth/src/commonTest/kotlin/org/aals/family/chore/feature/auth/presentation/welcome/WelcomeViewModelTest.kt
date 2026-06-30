package org.aals.family.chore.feature.auth.presentation.welcome

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.aals.family.chore.feature.auth.presentation.FakeTokenStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WelcomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: WelcomeViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WelcomeViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `clicking setup new family sends NavigateToSetupFamily event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(WelcomeAction.OnSetupNewFamilyClick)
            assertThat(awaitItem()).isEqualTo(WelcomeEvent.NavigateToSetupFamily)
        }
    }

    @Test
    fun `clicking join family sends NavigateToJoinFamily event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(WelcomeAction.OnJoinFamilyClick)
            assertThat(awaitItem()).isEqualTo(WelcomeEvent.NavigateToJoinFamily)
        }
    }
}
