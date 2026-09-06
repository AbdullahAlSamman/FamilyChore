package org.aals.family.chore.feature.dashboard.presentation

import androidx.compose.runtime.Immutable
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.model.BehaviorItem
import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.presentation.UiText
import org.aals.family.chore.feature.dashboard.presentation.navigation.DashboardTabRoute

@Immutable
data class AddChoreFormState(
    val nameError: UiText? = null,
    val pointsError: UiText? = null,
)

@Immutable
data class AddMemberFormState(
    val nickname: String = "",
    val nicknameError: UiText? = null,
    val role: UserRole = UserRole.CHILD,
    val pin: String = "",
    val isAdding: Boolean = false,
)

sealed interface DashboardState {
    data object Loading : DashboardState

    @Immutable
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
        val addChoreForm: AddChoreFormState = AddChoreFormState(),
        val addMemberForm: AddMemberFormState = AddMemberFormState(),
        val inviteQrContent: String? = null,
    ) : DashboardState

    data class Error(val message: UiText) : DashboardState
}
