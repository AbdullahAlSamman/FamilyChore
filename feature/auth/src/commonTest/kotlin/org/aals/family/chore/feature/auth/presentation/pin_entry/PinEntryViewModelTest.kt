package org.aals.family.chore.feature.auth.presentation.pin_entry

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.aals.family.chore.feature.auth.presentation.FakeAuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PinEntryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PinEntryViewModel
    private lateinit var authRepository: FakeAuthRepository
    private val userId = "user123"

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submitting 4 digit pin in setup mode calls setupPin`() = runTest {
        viewModel = PinEntryViewModel(authRepository, SavedStateHandle(mapOf("userId" to userId, "isSetupMode" to true)))

        viewModel.events.test {
            viewModel.onAction(PinEntryAction.OnPinChange("1234"))
            viewModel.onAction(PinEntryAction.OnSubmit)
            assertThat(awaitItem()).isEqualTo(PinEntryEvent.PinVerified)
        }
    }

    @Test
    fun `submitting short pin sets error`() = runTest {
        viewModel = PinEntryViewModel(authRepository, SavedStateHandle(mapOf("userId" to userId)))

        viewModel.state.test {
            assertThat(awaitItem().pin).isEqualTo("")
            viewModel.onAction(PinEntryAction.OnPinChange("123"))
            assertThat(awaitItem().pin).isEqualTo("123")
            viewModel.onAction(PinEntryAction.OnSubmit)
            assertThat(awaitItem().error).isEqualTo("PIN must be 4 digits")
        }
    }
}
