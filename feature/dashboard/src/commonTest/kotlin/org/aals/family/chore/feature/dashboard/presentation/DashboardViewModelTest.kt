package org.aals.family.chore.feature.dashboard.presentation

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
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.feature.dashboard.domain.model.BehaviorDefaults
import org.aals.family.chore.feature.dashboard.presentation.navigation.BehaviorRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentOverviewRoute
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var choreRepository: FakeChoreRepository
    private lateinit var connectivityRepository: FakeConnectivityRepository
    private lateinit var tokenStorage: FakeTokenStorage
    private lateinit var timeProvider: FakeTimeProvider
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        transactionRepository = FakeTransactionRepository()
        choreRepository = FakeChoreRepository()
        connectivityRepository = FakeConnectivityRepository()
        tokenStorage = FakeTokenStorage()
        timeProvider = FakeTimeProvider()
        
        // Default mock setup
        authRepository.currentUser = User("parent1", "family1", "Parent", UserRole.PARENT)
        authRepository.familyMembers = mutableListOf(
            User("child1", "family1", "Alice", UserRole.CHILD, 100),
            User("child2", "family1", "Bob", UserRole.CHILD, 50)
        )
        
        viewModel = DashboardViewModel(
            authRepository = authRepository,
            transactionRepository = transactionRepository,
            choreRepository = choreRepository,
            connectivityRepository = connectivityRepository,
            tokenStorage = tokenStorage,
            logger = Logger.withTag("DashboardViewModelTest"),
            timeProvider = timeProvider
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Success for parent`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state is DashboardState.Success).isEqualTo(expected = true)
            val successState = state as DashboardState.Success
            assertThat(successState.user.role).isEqualTo(UserRole.PARENT)
            assertThat(successState.currentTab).isEqualTo(ParentOverviewRoute)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state loads family members`() = runTest {
        viewModel.state.test {
            val state = awaitItem() as DashboardState.Success
            assertThat(state.familyMembers.size).isEqualTo(2)
            assertThat(state.familyMembers[0].nickname).isEqualTo("Alice")
            assertThat(state.selectedAssigneeId).isEqualTo("child1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ChangeTab action updates state`() = runTest {
        viewModel.state.test {
            awaitItem() // Skip initial state
            viewModel.onAction(DashboardAction.ChangeTab(BehaviorRoute))
            val state = awaitItem() as DashboardState.Success
            assertThat(state.currentTab).isEqualTo(BehaviorRoute)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `AwardPoints action adds transaction`() = runTest {
        val behaviorItem = BehaviorDefaults.defaultItems.first()
        viewModel.onAction(DashboardAction.AwardPoints("child1", behaviorItem))
        
        assertThat(transactionRepository.addedTransactions.size).isEqualTo(1)
        assertThat(transactionRepository.addedTransactions[0].userId).isEqualTo("child1")
        assertThat(transactionRepository.addedTransactions[0].amount).isEqualTo(behaviorItem.points)
    }

    @Test
    fun `CreateChore action adds chore to repository`() = runTest {
        viewModel.state.test {
            awaitItem() // Initial state
            val action = DashboardAction.CreateChore(
                name = "Wash Dishes",
                points = 20,
                description = null,
                assignedTo = "child1"
            )
            viewModel.onAction(action)
            
            assertThat(choreRepository.createdChores.size).isEqualTo(1)
            assertThat(choreRepository.createdChores[0].name).isEqualTo("Wash Dishes")
            assertThat(choreRepository.createdChores[0].assignedTo).isEqualTo("child1")
            
            val successState = awaitItem() as DashboardState.Success
            assertThat(successState.chores.any { it.name == "Wash Dishes" }).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `CreateChore with empty name shows error`() = runTest {
        viewModel.state.test {
            awaitItem() // Initial state
            val action = DashboardAction.CreateChore(
                name = "",
                points = 20,
                description = null,
                assignedTo = "child1"
            )
            viewModel.onAction(action)
            
            val state = awaitItem() as DashboardState.Success
            assertThat(state.choreNameError != null).isEqualTo(true)
            assertThat(choreRepository.createdChores.isEmpty()).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `CreateChore with zero points shows error`() = runTest {
        viewModel.state.test {
            awaitItem() // Initial state
            val action = DashboardAction.CreateChore(
                name = "Valid Name",
                points = 0,
                description = null,
                assignedTo = "child1"
            )
            viewModel.onAction(action)
            
            val state = awaitItem() as DashboardState.Success
            assertThat(state.chorePointsError != null).isEqualTo(true)
            assertThat(choreRepository.createdChores.isEmpty()).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnChoreNameChange clears error`() = runTest {
        viewModel.state.test {
            awaitItem() // Initial state
            
            // Trigger error
            viewModel.onAction(DashboardAction.CreateChore("", 10, null, "child1"))
            awaitItem()
            
            // Change name
            viewModel.onAction(DashboardAction.OnChoreNameChange("A"))
            val state = awaitItem() as DashboardState.Success
            assertThat(state.choreNameError).isEqualTo(null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Logout action sends Logout event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(DashboardAction.Logout)
            val event = awaitItem()
            assertThat(event is DashboardEvent.Logout).isEqualTo(true)
            val logoutEvent = event as DashboardEvent.Logout
            assertThat(logoutEvent.familyId).isEqualTo("family1")
            assertThat(logoutEvent.isServerOnline).isEqualTo(true)
        }
    }

    @Test
    fun `AddChild success adds user and shows QR`() = runTest {
        viewModel.state.test {
            awaitItem() // Initial Success
            viewModel.onAction(DashboardAction.AddChild("Charlie", true))
            
            // Should show loading state/flag if implemented, but here it's fast
            val stateAfterAdd = awaitItem() as DashboardState.Success
            assertThat(stateAfterAdd.inviteQrContent != null).isEqualTo(true)
            assertThat(authRepository.familyMembers.any { it.nickname == "Charlie" }).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ShowInviteQr generates QR content`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(DashboardAction.ShowInviteQr("child1"))
            val state = awaitItem() as DashboardState.Success
            assertThat(state.inviteQrContent != null).isEqualTo(true)
            assertThat(state.inviteQrContent!!.contains("child1")).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DismissInviteQr clears QR content`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(DashboardAction.ShowInviteQr("child1"))
            awaitItem()
            
            viewModel.onAction(DashboardAction.DismissInviteQr)
            val state = awaitItem() as DashboardState.Success
            assertThat(state.inviteQrContent).isEqualTo(null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ChangeLanguage action persists language and updates state`() = runTest {
        viewModel.state.test {
            awaitItem() // Initial Success
            
            viewModel.onAction(DashboardAction.ChangeLanguage(AppLanguage.ARABIC))
            
            // State should update via observation
            val state = awaitItem() as DashboardState.Success
            assertThat(state.language).isEqualTo(AppLanguage.ARABIC)
            assertThat(tokenStorage.language.value).isEqualTo(AppLanguage.ARABIC.isoCode)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `AddChild in offline mode adds user and does NOT show QR`() = runTest {
        tokenStorage.setOfflineMode(true)
        
        viewModel.onAction(DashboardAction.Refresh)

        viewModel.state.test {
            // Skip until we get Success with offline mode
            var state = awaitItem()
            while (state !is DashboardState.Success || !state.isOfflineMode) {
                state = awaitItem()
            }
            
            viewModel.onAction(DashboardAction.AddChild("Charlie", true))
            
            // Skip until adding is done and members are refreshed
            while (state !is DashboardState.Success || state.isAddingChild || state.familyMembers.none { it.nickname == "Charlie" }) {
                state = awaitItem()
            }
            
            val finalState = state as DashboardState.Success
            assertThat(finalState.inviteQrContent).isEqualTo(null)
            assertThat(authRepository.familyMembers.any { it.nickname == "Charlie" }).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `AddChild when server unreachable and NOT offline is blocked`() = runTest {
        connectivityRepository.isServerReachable.value = false
        tokenStorage.setOfflineMode(false)
        
        viewModel.state.test {
            awaitItem() // Skip initial Success
            
            viewModel.onAction(DashboardAction.AddChild("Charlie", true))
            
            // authRepository.addChildUser should NOT be called
            // Actually, in the current implementation, it just returns without doing anything.
            // So the state won't even change to "isAddingChild = true".
            
            assertThat(authRepository.familyMembers.any { it.nickname == "Charlie" }).isEqualTo(false)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Connectivity changes update state`() = runTest {
        viewModel.state.test {
            awaitItem()
            connectivityRepository.isServerReachable.value = false
            val state = awaitItem() as DashboardState.Success
            assertThat(state.isServerReachable).isEqualTo(false)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Error state when current user fails`() = runTest {
        authRepository.currentUser = null
        val newViewModel = DashboardViewModel(
            authRepository = authRepository,
            transactionRepository = transactionRepository,
            choreRepository = choreRepository,
            connectivityRepository = connectivityRepository,
            tokenStorage = tokenStorage,
            logger = Logger.withTag("DashboardViewModelTest"),
            timeProvider = timeProvider
        )
        newViewModel.state.test {
            val state = awaitItem()
            assertThat(state is DashboardState.Error).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
