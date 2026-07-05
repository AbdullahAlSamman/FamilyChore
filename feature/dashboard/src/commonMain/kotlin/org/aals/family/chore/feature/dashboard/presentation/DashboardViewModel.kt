package org.aals.family.chore.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.model.BehaviorItem
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.TransactionType
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TransactionRepository
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess
import org.aals.family.chore.feature.dashboard.presentation.navigation.ChildTodayRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentOverviewRoute

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val logger: Logger,
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state = _state.asStateFlow()

    internal val mockChildren = listOf(
        User("child1", "family1", "Alice", UserRole.CHILD, 100),
        User("child2", "family1", "Bob", UserRole.CHILD, 50)
    )

    internal val defaultBehaviorItems = listOf(
        BehaviorItem("1", "", "Politeness", 10),
        BehaviorItem("2", "", "Helping others", 15),
        BehaviorItem("3", "", "Rudeness", -10),
        BehaviorItem("4", "", "Ignoring instructions", -20)
    )

    init {
        loadDashboardData()
        observeConnectivity()
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.Refresh -> loadDashboardData(isRefreshing = true)
            DashboardAction.Logout -> {
                // Logout is handled by the Root/App level via callback
            }
            is DashboardAction.ChangeTab -> {
                val currentState = _state.value
                if (currentState is DashboardState.Success) {
                    _state.value = currentState.copy(currentTab = action.tab)
                }
            }
            is DashboardAction.SelectChild -> {
                val currentState = _state.value
                if (currentState is DashboardState.Success) {
                    _state.value = currentState.copy(selectedChildId = action.userId)
                }
            }
            is DashboardAction.AwardPoints -> awardPoints(action.targetUserId, action.item)
        }
    }

    private fun awardPoints(targetUserId: String, item: BehaviorItem) {
        val currentState = _state.value as? DashboardState.Success ?: return
        viewModelScope.launch {
            // TODO: kotlinx.datetime.Clock.System is currently unresolved due to a typealias clash with kotlin.time.Clock
            // in Kotlin 2.x/kotlinx-datetime 0.6.1+. Using 0L as a temporary fallback to allow the build to pass.
            val now = 0L 
            val transaction = Transaction(
                id = "tr_${targetUserId}_${item.id}_$now",
                familyId = currentState.user.familyId,
                userId = targetUserId,
                adminId = currentState.user.id,
                amount = item.points,
                type = if (item.points >= 0) TransactionType.BONUS else TransactionType.PENALTY,
                timestamp = now,
                note = item.name
            )

            transactionRepository.addTransaction(transaction)
                .onFailure { error ->
                    logger.e { "Failed to award points: $error" }
                }
        }
    }

    private fun loadDashboardData(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            if (isRefreshing) {
                val currentState = _state.value
                if (currentState is DashboardState.Success) {
                    _state.value = currentState.copy(isRefreshing = true)
                }
            } else {
                _state.value = DashboardState.Loading
            }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    // For now, we load transactions from the local database
                    transactionRepository.getTransactionsForUser(user.id)
                        .onEach { transactions ->
                            val currentState = _state.value
                            if (currentState is DashboardState.Success) {
                                _state.value = currentState.copy(
                                    user = user,
                                    transactions = transactions,
                                    isRefreshing = false
                                )
                            } else {
                                _state.value = DashboardState.Success(
                                    user = user,
                                    familyMembers = if (user.role == UserRole.PARENT) mockChildren + user else emptyList<User>(),
                                    selectedChildId = if (user.role == UserRole.PARENT) mockChildren.firstOrNull()?.id else null,
                                    transactions = transactions,
                                    behaviorItems = if (user.role == UserRole.PARENT) defaultBehaviorItems else emptyList<BehaviorItem>(),
                                    currentTab = if (user.role == UserRole.PARENT) ParentOverviewRoute else ChildTodayRoute
                                )
                            }
                        }
                        .launchIn(viewModelScope)
                }
                .onFailure { error ->
                    logger.e { "Failed to load dashboard data: $error" }
                    _state.value = DashboardState.Error("Failed to load profile. Please login again.")
                }
        }
    }

    private fun observeConnectivity() {
        connectivityRepository.isServerReachable
            .onEach { isReachable ->
                val currentState = _state.value
                if (currentState is DashboardState.Success) {
                    _state.value = currentState.copy(isServerReachable = isReachable)
                }
            }
            .launchIn(viewModelScope)
    }
}
