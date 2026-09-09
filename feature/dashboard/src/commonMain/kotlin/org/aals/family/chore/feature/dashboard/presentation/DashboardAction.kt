package org.aals.family.chore.feature.dashboard.presentation

import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.model.BehaviorItem
import org.aals.family.chore.core.domain.model.UserRole
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

    // Family Management
    data class OnMemberNicknameChange(val nickname: String) : DashboardAction
    data class ChangeNewMemberRole(val role: UserRole) : DashboardAction
    data class OnNewMemberPinChange(val pin: String) : DashboardAction
    data class AddMember(val nickname: String, val role: UserRole, val pin: String?) : DashboardAction
    data class UpdateUserPinRequirement(val userId: String, val requiresPin: Boolean) : DashboardAction
    data class ShowInviteQr(val userId: String? = null) : DashboardAction
    data object DismissInviteQr : DashboardAction

    data class ChangeLanguage(val language: AppLanguage) : DashboardAction
    data object NavigateToSettings : DashboardAction
}
