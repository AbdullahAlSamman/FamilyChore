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
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
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
    private lateinit var connectivityRepository: FakeConnectivityRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        transactionRepository = FakeTransactionRepository()
        connectivityRepository = FakeConnectivityRepository()
        
        // Default mock setup
        authRepository.currentUser = User("parent1", "family1", "Parent", UserRole.PARENT)
        
        viewModel = DashboardViewModel(
            authRepository = authRepository,
            transactionRepository = transactionRepository,
            connectivityRepository = connectivityRepository,
            logger = Logger.withTag("DashboardViewModelTest"),
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
        val behaviorItem = viewModel.defaultBehaviorItems.first()
        viewModel.onAction(DashboardAction.AwardPoints("child1", behaviorItem))
        
        assertThat(transactionRepository.addedTransactions.size).isEqualTo(1)
        assertThat(transactionRepository.addedTransactions[0].userId).isEqualTo("child1")
        assertThat(transactionRepository.addedTransactions[0].amount).isEqualTo(behaviorItem.points)
    }
}
