package org.aals.family.chore.feature.dashboard.presentation

import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.User

sealed interface DashboardState {
    data object Loading : DashboardState
    data class Success(
        val user: User,
        val transactions: List<Transaction> = emptyList(),
        val isServerReachable: Boolean = true,
        val isRefreshing: Boolean = false
    ) : DashboardState
    data class Error(val message: String) : DashboardState
}
