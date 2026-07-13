package org.aals.family.chore.feature.dashboard.presentation

import org.aals.family.chore.core.domain.model.BehaviorItem
import org.aals.family.chore.feature.dashboard.presentation.navigation.DashboardTabRoute

sealed interface DashboardAction {
    data object Refresh : DashboardAction
    data object Logout : DashboardAction
    data class ChangeTab(val tab: DashboardTabRoute) : DashboardAction
    data class AwardPoints(val targetUserId: String, val item: BehaviorItem) : DashboardAction
    data class SelectAssignee(val userId: String) : DashboardAction
    data class CreateChore(
        val name: String,
        val points: Int,
        val description: String?,
        val assignedTo: String
    ) : DashboardAction
    data class OnChoreNameChange(val name: String) : DashboardAction
    data class OnChorePointsChange(val points: String) : DashboardAction
}
