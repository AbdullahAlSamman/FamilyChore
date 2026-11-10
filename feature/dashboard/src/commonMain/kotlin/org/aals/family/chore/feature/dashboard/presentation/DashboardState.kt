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
    val dueDateError: UiText? = null,
    val timeWindowError: UiText? = null,
    val recurrence: org.aals.family.chore.core.domain.model.Recurrence = org.aals.family.chore.core.domain.model.Recurrence.NONE,
    val activeDays: Set<Int> = emptySet(),
    val dueDate: String = "",
    val activeWindowStart: String = "",
    val activeWindowEnd: String = "",
    val penalty: String = "0",
    val requiresPhotoProof: Boolean = false,
)

@Immutable
data class AddMemberFormState(
    val nickname: String = "",
    val nicknameError: UiText? = null,
    val role: UserRole = UserRole.CHILD,
    val pin: String = "",
    val pinError: UiText? = null,
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
        val pendingCounts: Map<String, Int> = emptyMap(),
        val currentTab: DashboardTabRoute,
        val isServerReachable: Boolean = true,
        val isOfflineMode: Boolean = false,
        val isRefreshing: Boolean = false,
        val isCameraAvailable: Boolean = false,
        val addChoreForm: AddChoreFormState = AddChoreFormState(),
        val addMemberForm: AddMemberFormState = AddMemberFormState(),
        val inviteQrContent: String? = null,
        val pinSetupTarget: User? = null,
        val pinSetupDraft: String = "",
        val isPinSetupVisible: Boolean = false,
        val pinSetupError: UiText? = null,
        val pinSetupSaving: Boolean = false,
        val isPinChangeMode: Boolean = false,
        val pictureTarget: User? = null,
        val showPresetPicker: Boolean = false,
        val cameraPermissionRequestCount: Int = 0,
        val showCameraCapture: Boolean = false,
        val photoProofChoreId: String? = null, // Which chore we are capturing proof for
        val isSavingPicture: Boolean = false,
        val memberPictures: Map<String, String> = emptyMap(),
        val choreImageTarget: String? = null, // Which chore we are setting an image for
        val showChorePresetPicker: Boolean = false,
        val isSavingChoreImage: Boolean = false,
        val choreImages: Map<String, String> = emptyMap(), // choreId -> iconName/path
    ) : DashboardState

    data class Error(val message: UiText) : DashboardState
}
