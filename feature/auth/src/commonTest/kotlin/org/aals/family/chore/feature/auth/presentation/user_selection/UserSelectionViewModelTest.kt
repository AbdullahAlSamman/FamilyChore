package org.aals.family.chore.feature.auth.presentation.user_selection

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.feature.auth.presentation.FakeAuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class UserSelectionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: UserSelectionViewModel
    private lateinit var authRepository: FakeAuthRepository
    private val pairingToken = "test_pairing_token"

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
    fun `initialization loads users`() = runTest {
        val users = listOf(User("1", "f1", "Child 1", UserRole.CHILD, 0))
        authRepository.users.addAll(users)
        
        viewModel = UserSelectionViewModel(authRepository, SavedStateHandle(mapOf("pairingToken" to pairingToken)))

        viewModel.state.test {
            assertThat(awaitItem().users).isEqualTo(users)
        }
    }

    @Test
    fun `clicking user confirms pairing and sends event`() = runTest {
        val user = User("1", "f1", "Child 1", UserRole.CHILD, 0)
        authRepository.users.add(user)
        viewModel = UserSelectionViewModel(authRepository, SavedStateHandle(mapOf("pairingToken" to pairingToken)))

        viewModel.events.test {
            viewModel.onAction(UserSelectionAction.OnUserClick(user))
            assertThat(awaitItem()).isEqualTo(UserSelectionEvent.PairingConfirmed(user.id))
        }
    }
}
