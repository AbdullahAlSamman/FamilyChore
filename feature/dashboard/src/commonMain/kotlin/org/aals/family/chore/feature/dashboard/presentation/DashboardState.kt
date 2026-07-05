package org.aals.family.chore.feature.dashboard.presentation

import org.aals.family.chore.core.domain.model.BehaviorItem
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.feature.dashboard.presentation.navigation.DashboardTabRoute

sealed interface DashboardState {
    data object Loading : DashboardState
    data class Success(
        val user: User,
        val familyMembers: List<User> = emptyList(),
        val selectedChildId: String? = null,
        val transactions: List<Transaction> = emptyList(),
        val behaviorItems: List<BehaviorItem> = emptyList(),
        val currentTab: DashboardTabRoute,
        val isServerReachable: Boolean = true,
        val isRefreshing: Boolean = false
    ) : DashboardState
    data class Error(val message: String) : DashboardState
}
