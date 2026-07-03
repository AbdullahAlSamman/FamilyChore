package org.aals.family.chore.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TransactionRepository
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val logger: Logger
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state = _state.asStateFlow()

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
                                    transactions = transactions
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
