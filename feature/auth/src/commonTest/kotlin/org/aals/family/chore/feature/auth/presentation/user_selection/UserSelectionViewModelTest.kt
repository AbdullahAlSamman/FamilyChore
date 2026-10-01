package org.aals.family.chore.feature.auth.presentation.user_selection

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.ProfilePictureRepository
import org.aals.family.chore.core.domain.usecase.ObserveConnectivityUseCase
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.EmptyResult
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.feature.auth.presentation.FakeAuthRepository
import org.aals.family.chore.feature.auth.presentation.FakeConnectivityRepository
import org.aals.family.chore.feature.auth.presentation.FakeTokenStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserSelectionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: UserSelectionViewModel
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var connectivityRepository: FakeConnectivityRepository
    private lateinit var tokenStorage: FakeTokenStorage
    private lateinit var observeConnectivityUseCase: ObserveConnectivityUseCase
    private val profilePictureRepository = FakeProfilePictureRepository()
    private val pairingToken = "test_pairing_token"

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        connectivityRepository = FakeConnectivityRepository()
        tokenStorage = FakeTokenStorage()
        observeConnectivityUseCase = ObserveConnectivityUseCase(tokenStorage, connectivityRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads users`() = runTest {
        val users = listOf(User("1", "f1", "Child 1", UserRole.CHILD, 0))
        authRepository.users.addAll(users)
        
        viewModel = UserSelectionViewModel(
            authRepository,
            observeConnectivityUseCase,
            SavedStateHandle(mapOf("pairingToken" to pairingToken)),
            profilePictureRepository,
            Logger.withTag("Test")
        )

        viewModel.state.test {
            // With UnconfinedTestDispatcher, the init block runs immediately.
            // We expect the final Success state.
            assertThat(awaitItem()).isEqualTo(UserSelectionState.Success(users, isFromDiscovery = true))
        }
    }

    @Test
    fun `clicking user confirms pairing and sends event`() = runTest {
        val user = User("1", "f1", "Child 1", UserRole.CHILD, 0)
        authRepository.users.add(user)
        viewModel = UserSelectionViewModel(
            authRepository,
            observeConnectivityUseCase,
            SavedStateHandle(mapOf("pairingToken" to pairingToken)),
            profilePictureRepository,
            Logger.withTag("Test")
        )

        viewModel.events.test {
            viewModel.onAction(UserSelectionAction.OnUserClick(user))
            assertThat(awaitItem()).isEqualTo(UserSelectionEvent.PairingConfirmed(user.id))
        }
    }
}

private class FakeProfilePictureRepository : ProfilePictureRepository {
    override suspend fun getProfilePicture(userId: String): String? = null
    override suspend fun saveCustomPicture(userId: String, sourcePath: String): EmptyResult<DataError.Local> = Result.Success(Unit)
    override suspend fun savePreset(userId: String, presetKey: String): EmptyResult<DataError.Local> = Result.Success(Unit)
}
