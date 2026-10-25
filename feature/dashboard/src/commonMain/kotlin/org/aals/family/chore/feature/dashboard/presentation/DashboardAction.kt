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
        val assignedTo: String,
        val recurrence: org.aals.family.chore.core.domain.model.Recurrence = org.aals.family.chore.core.domain.model.Recurrence.NONE,
        val activeDays: List<Int> = emptyList(),
        val activeWindowStart: Int? = null,
        val activeWindowEnd: Int? = null,
        val penalty: Int = 0,
        val requiresPhotoProof: Boolean = false,
        val iconName: String? = null
    ) : DashboardAction

    data class RequestApproval(val choreId: String) : DashboardAction
    data class ApproveChore(val choreId: String, val targetUserId: String) : DashboardAction
    data class DeclineChore(val choreId: String, val targetUserId: String) : DashboardAction

    data class OnChoreNameChange(val name: String) : DashboardAction
    data class OnChorePointsChange(val points: String) : DashboardAction
    data class OnChoreRecurrenceChange(val recurrence: org.aals.family.chore.core.domain.model.Recurrence) : DashboardAction
    data class OnChoreActiveDayToggle(val dayNumber: Int) : DashboardAction
    data class OnChoreWindowStartChange(val time: String) : DashboardAction
    data class OnChoreWindowEndChange(val time: String) : DashboardAction
    data class OnChorePenaltyChange(val penalty: String) : DashboardAction
    data class OnChoreRequiresPhotoProofToggle(val required: Boolean) : DashboardAction

    // Family Management
    data class OnMemberNicknameChange(val nickname: String) : DashboardAction
    data class ChangeNewMemberRole(val role: UserRole) : DashboardAction
    data class OnNewMemberPinChange(val pin: String) : DashboardAction
    data class AddMember(val nickname: String, val role: UserRole, val pin: String?) : DashboardAction
    data class UpdateUserPinRequirement(val userId: String, val requiresPin: Boolean) : DashboardAction
    data class ChangeMemberPin(val userId: String) : DashboardAction
    data class OnPinSetupDraftChange(val pin: String) : DashboardAction
    data object TogglePinSetupVisibility : DashboardAction
    data class ConfirmChildPinSetup(val userId: String, val pin: String, val enableRequiresPin: Boolean) : DashboardAction
    data object DismissChildPinSetup : DashboardAction
    data class ShowInviteQr(val userId: String? = null) : DashboardAction
    data object DismissInviteQr : DashboardAction

    data class ChangeLanguage(val language: AppLanguage) : DashboardAction
    data object NavigateToSettings : DashboardAction

    // Profile Pictures
    data class ShowPictureSourceDialog(val userId: String) : DashboardAction
    data object DismissPictureSourceDialog : DashboardAction
    data object ShowPresetPicker : DashboardAction
    data object DismissPresetPicker : DashboardAction
    data class SelectPresetAvatar(val userId: String, val preset: String) : DashboardAction
    data class OnPickedImage(val userId: String, val imageBytes: ByteArray) : DashboardAction
    data class OnCameraPermissionResult(val granted: Boolean) : DashboardAction
    data object StartCameraCapture : DashboardAction
    data object CancelCameraCapture : DashboardAction
    
    data class StartPhotoProofCapture(val choreId: String) : DashboardAction
    data class OnPhotoProofCaptured(val choreId: String, val imageBytes: ByteArray) : DashboardAction
}
