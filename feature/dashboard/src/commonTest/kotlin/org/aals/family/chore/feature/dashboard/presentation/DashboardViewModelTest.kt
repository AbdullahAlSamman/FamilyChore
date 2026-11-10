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
import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.ChoreStatus
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.HardwareAvailabilityRepository
import org.aals.family.chore.core.domain.repository.ProfilePictureRepository
import org.aals.family.chore.core.domain.usecase.ObserveConnectivityUseCase
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.EmptyResult
import org.aals.family.chore.core.domain.util.Result
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
    private lateinit var observeConnectivityUseCase: ObserveConnectivityUseCase
    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var profilePictureRepository: FakeProfilePictureRepository
    private lateinit var hardwareAvailabilityRepository: FakeHardwareAvailabilityRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        transactionRepository = FakeTransactionRepository()
        choreRepository = FakeChoreRepository()
        connectivityRepository = FakeConnectivityRepository()
        tokenStorage = FakeTokenStorage()
        observeConnectivityUseCase = ObserveConnectivityUseCase(tokenStorage, connectivityRepository)
        timeProvider = FakeTimeProvider()
        profilePictureRepository = FakeProfilePictureRepository()
        hardwareAvailabilityRepository = FakeHardwareAvailabilityRepository()
        
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
            observeConnectivityUseCase = observeConnectivityUseCase,
            choreGenerationUseCase = org.aals.family.chore.core.domain.usecase.ChoreGenerationUseCase(choreRepository, timeProvider),
            tokenStorage = tokenStorage,
            profilePictureRepository = profilePictureRepository,
            hardwareAvailabilityRepository = hardwareAvailabilityRepository,
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
    fun `Load dashboard computes pendingCounts correctly for parent and child`() = runTest {
        val now = 1000L
        timeProvider.staticTime = now
        val parentPendingChore = Chore(
            id = "chore_child1_1", familyId = "family1", name = "Test 1", description = null, points = 10,
            status = ChoreStatus.PENDING_APPROVAL, assignedTo = "child1", createdBy = "parent1", createdAt = now, updatedAt = now
        )
        val childPendingChore = Chore(
            id = "chore_child2_1", familyId = "family1", name = "Test 2", description = null, points = 10,
            status = ChoreStatus.PENDING, assignedTo = "child2", createdBy = "parent1", createdAt = now, updatedAt = now
        )
        choreRepository.chores.value = listOf(parentPendingChore, childPendingChore)

        // Test as PARENT
        viewModel.onAction(DashboardAction.Refresh)
        viewModel.state.test {
            val state = awaitItem()
            if (state is DashboardState.Success) {
                assertThat(state.pendingCounts["tasks"]).isEqualTo(1)
                assertThat(state.pendingCounts["child1"]).isEqualTo(1)
                assertThat(state.pendingCounts["child2"] == null).isEqualTo(true)
            } else {
                val nextState = awaitItem() as DashboardState.Success
                assertThat(nextState.pendingCounts["tasks"]).isEqualTo(1)
                assertThat(nextState.pendingCounts["child1"]).isEqualTo(1)
                assertThat(nextState.pendingCounts["child2"] == null).isEqualTo(true)
            }
            cancelAndIgnoreRemainingEvents()
        }

        // Test as CHILD
        authRepository.currentUser = User("child2", "family1", "Bob", UserRole.CHILD, 50)
        viewModel.onAction(DashboardAction.Refresh)
        viewModel.state.test {
            var state = awaitItem()
            if (state !is DashboardState.Success || state.user.role != UserRole.CHILD) {
                state = awaitItem()
            }
            val successState = state as DashboardState.Success
            assertThat(successState.pendingCounts["today"]).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Load dashboard invokes ChoreGenerationUseCase and creates daily instances`() = runTest {
        val now = 1000L
        timeProvider.staticTime = now
        val template = Chore(
            id = "template1", familyId = "family1", name = "Daily Chore", description = null, points = 10,
            status = ChoreStatus.PENDING, assignedTo = "child1", createdBy = "parent1", createdAt = now, updatedAt = now,
            recurrence = org.aals.family.chore.core.domain.model.Recurrence.DAILY
        )
        choreRepository.chores.value = listOf(template)

        viewModel.onAction(DashboardAction.Refresh)
        viewModel.state.test {
            var state = awaitItem()
            while (state !is DashboardState.Success) {
                state = awaitItem()
            }
            // Wait for coroutines to settle and generation to finish
            kotlinx.coroutines.delay(50) 
            
            // Check that the generated instance(s) are added to the repository
            // (ChoreGenerationUseCase should add them via createChore)
            assertThat(choreRepository.createdChores.isNotEmpty()).isEqualTo(true)
            val generated = choreRepository.createdChores.first()
            assertThat(generated.parentChoreId).isEqualTo("template1")
            
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
    fun `ApproveChore adds transaction and updates member points`() = runTest {
        val now = 1000L
        timeProvider.staticTime = now
        val pendingChore = Chore(
            id = "chore_child1_1", familyId = "family1", name = "Clean Room", description = null, points = 20,
            status = ChoreStatus.PENDING_APPROVAL, assignedTo = "child1", createdBy = "parent1", createdAt = now, updatedAt = now
        )
        choreRepository.chores.value = listOf(pendingChore)

        viewModel.onAction(DashboardAction.Refresh)
        viewModel.state.test {
            awaitItem() // Skip initial state
            
            viewModel.onAction(DashboardAction.ApproveChore("chore_child1_1", "child1"))
            
            val state = awaitItem() as DashboardState.Success
            val child1 = state.familyMembers.find { it.id == "child1" }
            assertThat(child1?.points).isEqualTo(20)
            
            cancelAndIgnoreRemainingEvents()
        }
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
            assertThat(state.addChoreForm.nameError != null).isEqualTo(true)
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
            assertThat(state.addChoreForm.pointsError != null).isEqualTo(true)
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
            assertThat(state.addChoreForm.nameError).isEqualTo(null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Time window validation sets error when end time is before start time`() = runTest {
        viewModel.state.test {
            awaitItem() // Initial state
            
            viewModel.onAction(DashboardAction.OnChoreWindowStartChange("13:00"))
            awaitItem()
            
            viewModel.onAction(DashboardAction.OnChoreWindowEndChange("12:00"))
            val state = awaitItem() as DashboardState.Success
            assertThat(state.addChoreForm.timeWindowError != null).isEqualTo(true)
            
            // Attempt creation - should be blocked
            val createAction = DashboardAction.CreateChore(
                name = "Valid Chore",
                points = 10,
                description = null,
                assignedTo = "child1",
                recurrence = org.aals.family.chore.core.domain.model.Recurrence.CUSTOM
            )
            viewModel.onAction(createAction)
            assertThat(choreRepository.createdChores.isEmpty()).isEqualTo(true)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Due date validation sets error when date is in the past`() = runTest {
        timeProvider.staticTime = 1761400000000L
        viewModel.state.test {
            awaitItem() // Initial state
            
            // Set past date
            viewModel.onAction(DashboardAction.OnChoreDueDateChange("2020-01-01"))
            val state = awaitItem() as DashboardState.Success
            assertThat(state.addChoreForm.dueDateError != null).isEqualTo(true)
            
            // Attempt creation - should be blocked
            val createAction = DashboardAction.CreateChore(
                name = "Past Chore",
                points = 10,
                description = null,
                assignedTo = "child1",
                recurrence = org.aals.family.chore.core.domain.model.Recurrence.CUSTOM
            )
            viewModel.onAction(createAction)
            assertThat(choreRepository.createdChores.isEmpty()).isEqualTo(true)
            
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
    fun `AddMember success adds user and shows QR`() = runTest {
        viewModel.state.test {
            awaitItem() // Initial Success
            viewModel.onAction(DashboardAction.AddMember("Charlie", UserRole.CHILD, "1234"))
            
            // Should show loading state/flag if implemented, but here it's fast
            val stateAfterAdd = awaitItem() as DashboardState.Success
            assertThat(stateAfterAdd.inviteQrContent != null).isEqualTo(true)
            assertThat(authRepository.familyMembers.any { it.nickname == "Charlie" }).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `AddMember parent enforces PIN and shows QR`() = runTest {
        viewModel.state.test {
            awaitItem() // Initial Success
            // Attempt to add parent with PIN
            viewModel.onAction(DashboardAction.AddMember("NewParent", UserRole.PARENT, "1234"))
            
            val stateAfterAdd = awaitItem() as DashboardState.Success
            val newParent = authRepository.familyMembers.find { it.nickname == "NewParent" }
            assertThat(newParent != null).isEqualTo(true)
            assertThat(newParent!!.role).isEqualTo(UserRole.PARENT)
            assertThat(newParent.requiresPin).isEqualTo(true)
            assertThat(stateAfterAdd.inviteQrContent != null).isEqualTo(true)
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
    fun `AddMember in offline mode adds user and does NOT show QR`() = runTest {
        tokenStorage.setOfflineMode(true)
        
        viewModel.onAction(DashboardAction.Refresh)

        viewModel.state.test {
            // Skip until we get Success with offline mode
            var state = awaitItem()
            while (state !is DashboardState.Success || !state.isOfflineMode) {
                state = awaitItem()
            }
            
            viewModel.onAction(DashboardAction.AddMember("Charlie", UserRole.CHILD, "1234"))
            
            // Skip until adding is done and members are refreshed
            while (state !is DashboardState.Success || state.addMemberForm.isAdding || state.familyMembers.none { it.nickname == "Charlie" }) {
                state = awaitItem()
            }
            
            val finalState = state as DashboardState.Success
            assertThat(finalState.inviteQrContent).isEqualTo(null)
            assertThat(authRepository.familyMembers.any { it.nickname == "Charlie" }).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `AddMember when server unreachable and NOT offline is blocked`() = runTest {
        connectivityRepository.isServerReachable.value = false
        tokenStorage.setOfflineMode(false)
        
        viewModel.state.test {
            awaitItem() // Skip initial Success
            
            viewModel.onAction(DashboardAction.AddMember("Charlie", UserRole.CHILD, "1234"))
            
            // authRepository.addFamilyMember should NOT be called
            // Actually, in the current implementation, it just returns without doing anything.
            // So the state won't even change to "isAddingMember = true".
            
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
            observeConnectivityUseCase = observeConnectivityUseCase,
            choreGenerationUseCase = org.aals.family.chore.core.domain.usecase.ChoreGenerationUseCase(choreRepository, timeProvider),
            tokenStorage = tokenStorage,
            profilePictureRepository = profilePictureRepository,
            hardwareAvailabilityRepository = hardwareAvailabilityRepository,
            logger = Logger.withTag("DashboardViewModelTest"),
            timeProvider = timeProvider
        )
        newViewModel.state.test {
            val state = awaitItem()
            assertThat(state is DashboardState.Error).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `enabling requiresPin for child opens pin setup dialog`() = runTest {
        viewModel.state.test {
            awaitItem() // initial
            viewModel.onAction(DashboardAction.UpdateUserPinRequirement("child1", true))
            val state = awaitItem() as DashboardState.Success
            assertThat(state.pinSetupTarget?.id).isEqualTo("child1")
            // The flag should NOT be toggled until a PIN is set
            assertThat(authRepository.updatePinRequirementCalls.isEmpty()).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirming child pin setup calls setupPin and enables requirement`() = runTest {
        viewModel.state.test {
            awaitItem() // initial
            viewModel.onAction(DashboardAction.UpdateUserPinRequirement("child1", true))
            awaitItem() // dialog opened
            viewModel.onAction(DashboardAction.ConfirmChildPinSetup("child1", "12345", enableRequiresPin = true))

            assertThat(authRepository.setupPinCalls.size).isEqualTo(1)
            assertThat(authRepository.setupPinCalls[0].second).isEqualTo("12345")
            assertThat(authRepository.updatePinRequirementCalls.contains("child1" to true)).isEqualTo(true)

            val state = viewModel.state.value as DashboardState.Success
            assertThat(state.pinSetupTarget).isEqualTo(null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirming child pin setup with invalid length sets error`() = runTest {
        viewModel.state.test {
            awaitItem() // initial
            viewModel.onAction(DashboardAction.UpdateUserPinRequirement("child1", true))
            awaitItem() // dialog opened
            viewModel.onAction(DashboardAction.ConfirmChildPinSetup("child1", "123", enableRequiresPin = true))

            val state = viewModel.state.value as DashboardState.Success
            assertThat(state.pinSetupError != null).isEqualTo(true)
            assertThat(authRepository.setupPinCalls.isEmpty()).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ChangeMemberPin opens dialog in change mode`() = runTest {
        viewModel.state.test {
            awaitItem() // initial
            viewModel.onAction(DashboardAction.ChangeMemberPin("child1"))
            val state = awaitItem() as DashboardState.Success
            assertThat(state.pinSetupTarget?.id).isEqualTo("child1")
            assertThat(state.isPinChangeMode).isEqualTo(true)
            assertThat(authRepository.updatePinRequirementCalls.isEmpty()).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ChangeMemberPin for parent member opens dialog`() = runTest {
        authRepository.familyMembers.add(User("parent1", "family1", "Parent", UserRole.PARENT))
        authRepository.familyMembers.add(User("parent2", "family1", "OtherParent", UserRole.PARENT))
        val newViewModel = DashboardViewModel(
            authRepository = authRepository,
            transactionRepository = transactionRepository,
            choreRepository = choreRepository,
            observeConnectivityUseCase = observeConnectivityUseCase,
            choreGenerationUseCase = org.aals.family.chore.core.domain.usecase.ChoreGenerationUseCase(choreRepository, timeProvider),
            tokenStorage = tokenStorage,
            profilePictureRepository = profilePictureRepository,
            hardwareAvailabilityRepository = hardwareAvailabilityRepository,
            logger = Logger.withTag("DashboardViewModelTest"),
            timeProvider = timeProvider
        )
        testDispatcher.scheduler.advanceUntilIdle()

        newViewModel.onAction(DashboardAction.ChangeMemberPin("parent1"))

        val state = newViewModel.state.value as DashboardState.Success
        assertThat(state.pinSetupTarget?.id).isEqualTo("parent1")
        assertThat(state.isPinChangeMode).isEqualTo(true)
    }

    @Test
    fun `confirming pin change for another parent does not emit Logout event`() = runTest {
        authRepository.familyMembers.add(User("parent2", "family1", "OtherParent", UserRole.PARENT))
        viewModel.onAction(DashboardAction.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(DashboardAction.ChangeMemberPin("parent2"))
            viewModel.onAction(DashboardAction.ConfirmChildPinSetup("parent2", "5678", enableRequiresPin = false))

            val event = awaitItem()
            assertThat(event is DashboardEvent.ShowMessage).isEqualTo(true)
        }
    }

    @Test
    fun `confirming pin change for another user does not enable requirement and shows message`() = runTest {
        viewModel.events.test {
            viewModel.onAction(DashboardAction.ChangeMemberPin("child1"))
            viewModel.onAction(DashboardAction.ConfirmChildPinSetup("child1", "5678", enableRequiresPin = false))

            assertThat(authRepository.setupPinCalls.size).isEqualTo(1)
            assertThat(authRepository.setupPinCalls[0].second).isEqualTo("5678")
            // Reset must NOT touch requiresPin
            assertThat(authRepository.updatePinRequirementCalls.isEmpty()).isEqualTo(true)

            val event = awaitItem()
            assertThat(event is DashboardEvent.ShowMessage).isEqualTo(true)
        }
        val state = viewModel.state.value as DashboardState.Success
        assertThat(state.pinSetupTarget).isEqualTo(null)
        assertThat(state.isPinChangeMode).isEqualTo(false)
    }

    @Test
    fun `confirming pin change for own user emits Logout event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(DashboardAction.ChangeMemberPin("parent1"))
            viewModel.onAction(DashboardAction.ConfirmChildPinSetup("parent1", "9999", enableRequiresPin = false))

            assertThat(authRepository.setupPinCalls.size).isEqualTo(1)
            assertThat(authRepository.setupPinCalls[0].second).isEqualTo("9999")

            val event = awaitItem()
            assertThat(event is DashboardEvent.Logout).isEqualTo(true)
            val logoutEvent = event as DashboardEvent.Logout
            assertThat(logoutEvent.familyId).isEqualTo("family1")
            assertThat(logoutEvent.isServerOnline).isEqualTo(true)
        }
    }

    @Test
    fun `ShowPictureSourceDialog sets pictureTarget`() = runTest {
        viewModel.state.test {
            awaitItem() // initial
            viewModel.onAction(DashboardAction.ShowPictureSourceDialog("child1"))
            val state = awaitItem() as DashboardState.Success
            assertThat(state.pictureTarget?.id).isEqualTo("child1")
            assertThat(state.showPresetPicker).isEqualTo(false)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DismissPictureSourceDialog clears pictureTarget`() = runTest {
        viewModel.state.test {
            awaitItem() // initial
            viewModel.onAction(DashboardAction.ShowPictureSourceDialog("child1"))
            awaitItem()
            viewModel.onAction(DashboardAction.DismissPictureSourceDialog)
            val state = awaitItem() as DashboardState.Success
            assertThat(state.pictureTarget).isEqualTo(null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ShowPresetPicker after source dialog opens preset picker`() = runTest {
        viewModel.state.test {
            awaitItem() // initial
            viewModel.onAction(DashboardAction.ShowPictureSourceDialog("child1"))
            awaitItem()
            viewModel.onAction(DashboardAction.ShowPresetPicker)
            val state = awaitItem() as DashboardState.Success
            assertThat(state.showPresetPicker).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SelectPresetAvatar saves preset and updates member pictures`() = runTest {
        viewModel.state.test {
            awaitItem() // initial
            viewModel.events.test {
                viewModel.onAction(DashboardAction.SelectPresetAvatar("child1", "1"))

                assertThat(profilePictureRepository.presetSaves.size).isEqualTo(1)
                assertThat(profilePictureRepository.presetSaves[0]).isEqualTo("child1" to "1")

                val event = awaitItem()
                assertThat(event is DashboardEvent.ShowMessage).isEqualTo(true)
            }

            val state = viewModel.state.value as DashboardState.Success
            assertThat(state.pictureTarget).isEqualTo(null)
            assertThat(state.memberPictures["child1"]).isEqualTo("bundled:1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnPickedImage saves custom picture and updates member pictures`() = runTest {
        viewModel.state.test {
            awaitItem() // initial
            viewModel.events.test {
                val fakeImageBytes = byteArrayOf(1, 2, 3)
                viewModel.onAction(DashboardAction.OnPickedImage("child1", fakeImageBytes))

                assertThat(profilePictureRepository.customSaves.size).isEqualTo(1)
                assertThat(profilePictureRepository.customSaves[0].first).isEqualTo("child1")
                assertThat(profilePictureRepository.customSaves[0].second).isEqualTo(fakeImageBytes)

                val event = awaitItem()
                assertThat(event is DashboardEvent.ShowMessage).isEqualTo(true)
            }

            val state = viewModel.state.value as DashboardState.Success
            assertThat(state.pictureTarget).isEqualTo(null)
            assertThat(state.memberPictures["child1"]).isEqualTo("/fake/path/for/child1.jpg")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ShowChoreImageSourceDialog sets choreImageTarget`() = runTest {
        choreRepository.chores.value = listOf(
            Chore("chore1", "family1", "Clean Room", null, 10, ChoreStatus.PENDING, "child1", "parent1", 0L, 0L)
        )
        viewModel.state.test {
            awaitItem() // initial
            viewModel.onAction(DashboardAction.ShowChoreImageSourceDialog("chore1"))
            val state = awaitItem() as DashboardState.Success
            assertThat(state.choreImageTarget).isEqualTo("chore1")
            assertThat(state.showChorePresetPicker).isEqualTo(false)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SelectChorePreset saves preset image for chore`() = runTest {
        choreRepository.chores.value = listOf(
            Chore("chore1", "family1", "Clean Room", null, 10, ChoreStatus.PENDING, "child1", "parent1", 0L, 0L)
        )
        viewModel.state.test {
            awaitItem() // initial
            viewModel.events.test {
                viewModel.onAction(DashboardAction.SelectChorePreset("chore1", "clean"))
                val event = awaitItem()
                assertThat(event is DashboardEvent.ShowMessage).isEqualTo(true)
            }
            val state = awaitItem() as DashboardState.Success
            assertThat(state.choreImageTarget).isEqualTo(null)
            assertThat(state.choreImages["chore1"]).isEqualTo("bundled:clean")
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeHardwareAvailabilityRepository : HardwareAvailabilityRepository {
    var hasCameraResult: Boolean = true

    override fun hasCamera(): Boolean = hasCameraResult
}

private class FakeProfilePictureRepository : ProfilePictureRepository {
    val pictures = mutableMapOf<String, String>()
    val presetSaves = mutableListOf<Pair<String, String>>()
    val customSaves = mutableListOf<Pair<String, ByteArray>>()

    override suspend fun getProfilePicture(userId: String): String? = pictures[userId]

    override suspend fun saveCustomPicture(userId: String, imageBytes: ByteArray): EmptyResult<DataError.Local> {
        customSaves.add(userId to imageBytes)
        // Store a fake path to represent the saved bytes in tests
        val fakePath = "/fake/path/for/${userId}.jpg"
        pictures[userId] = fakePath
        return Result.Success(Unit)
    }

    override suspend fun savePreset(userId: String, presetKey: String): EmptyResult<DataError.Local> {
        presetSaves.add(userId to presetKey)
        pictures[userId] = "bundled:$presetKey"
        return Result.Success(Unit)
    }
}
