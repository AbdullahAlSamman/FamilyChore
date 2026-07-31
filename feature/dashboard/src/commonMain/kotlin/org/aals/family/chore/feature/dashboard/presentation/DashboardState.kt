package org.aals.family.chore.feature.dashboard.presentation

import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.model.BehaviorItem
import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.presentation.UiText
import org.aals.family.chore.feature.dashboard.presentation.navigation.DashboardTabRoute

sealed interface DashboardState {
    data object Loading : DashboardState
    data class Success(
        val user: User,
        val language: AppLanguage = AppLanguage.ENGLISH,
        val familyMembers: List<User> = emptyList(),
        val selectedAssigneeId: String? = null,
        val transactions: List<Transaction> = emptyList(),
        val chores: List<Chore> = emptyList(),
        val behaviorItems: List<BehaviorItem> = emptyList(),
        val currentTab: DashboardTabRoute,
        val isServerReachable: Boolean = true,
        val isOfflineMode: Boolean = false,
        val isRefreshing: Boolean = false,
        // Validation states for Add Chore form
        val choreNameError: UiText? = null,
        val chorePointsError: UiText? = null,
        // Family Management
        val newMemberNickname: String = "",
        val memberNicknameError: UiText? = null,
        val newMemberRole: UserRole = UserRole.CHILD,
        val requiresPinForNewMember: Boolean = true,
        val isAddingMember: Boolean = false,
        val inviteQrContent: String? = null
    ) : DashboardState
    data class Error(val message: UiText) : DashboardState
}
