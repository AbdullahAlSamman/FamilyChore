package org.aals.family.chore.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.child_pin_set_success
import familychore.core.generated.resources.chore_image_updated_success
import familychore.core.generated.resources.dashboard_load_failed_error
import familychore.core.generated.resources.member_pin_change_success
import familychore.core.generated.resources.picture_updated_success
import familychore.core.generated.resources.time_window_invalid_error
import familychore.core.generated.resources.due_date_past_error
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.model.BehaviorItem
import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.ChoreStatus
import org.aals.family.chore.core.domain.model.PairingToken
import org.aals.family.chore.core.domain.model.Recurrence
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.TransactionType
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.ChoreRepository
import org.aals.family.chore.core.domain.repository.HardwareAvailabilityRepository
import org.aals.family.chore.core.domain.repository.ProfilePictureRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.repository.TransactionRepository
import org.aals.family.chore.core.domain.usecase.ChoreGenerationUseCase
import org.aals.family.chore.core.domain.usecase.ObserveConnectivityUseCase
import org.aals.family.chore.core.domain.util.TimeProvider
import org.aals.family.chore.core.domain.util.getOrElse
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess
import org.aals.family.chore.core.domain.util.isDateInPast
import org.aals.family.chore.core.domain.util.parseDateToEpochMillis
import org.aals.family.chore.core.domain.util.startOfTodayEpochMillis
import org.aals.family.chore.core.domain.util.toMinutesOfDay
import org.aals.family.chore.core.domain.validation.AuthValidator
import org.aals.family.chore.core.domain.validation.ChoreValidator
import org.aals.family.chore.core.presentation.UiText
import org.aals.family.chore.core.presentation.toUiText
import org.aals.family.chore.feature.dashboard.domain.model.BehaviorDefaults
import org.aals.family.chore.feature.dashboard.presentation.navigation.ChildTodayRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentOverviewRoute

sealed interface DashboardEvent {
    data class Logout(val isServerOnline: Boolean, val familyId: String?) : DashboardEvent
    data object NavigateToSettings : DashboardEvent
    data class ShowMessage(val message: UiText) : DashboardEvent
}

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val choreRepository: ChoreRepository,
    private val observeConnectivityUseCase: ObserveConnectivityUseCase,
    private val choreGenerationUseCase: ChoreGenerationUseCase,
    private val tokenStorage: TokenStorage,
    private val profilePictureRepository: ProfilePictureRepository,
    private val hardwareAvailabilityRepository: HardwareAvailabilityRepository,
    private val logger: Logger,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state = _state.asStateFlow()

    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadDashboardData()
        observeConnectivity()
        observeLanguage()
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.Refresh -> loadDashboardData(isRefreshing = true)
            DashboardAction.Logout -> {
                val currentState = _state.value
                val isOnline = (currentState as? DashboardState.Success)?.isServerReachable ?: false
                val familyId = (currentState as? DashboardState.Success)?.user?.familyId
                viewModelScope.launch {
                    _events.send(DashboardEvent.Logout(isOnline, familyId))
                }
            }
            is DashboardAction.ChangeTab -> {
                updateSuccessState { it.copy(currentTab = action.tab) }
            }
            is DashboardAction.SelectAssignee -> {
                updateSuccessState { it.copy(selectedAssigneeId = action.userId) }
            }
            is DashboardAction.AwardPoints -> awardPoints(action.targetUserId, action.item)
            is DashboardAction.CreateChore -> createChore(action)
            is DashboardAction.RequestApproval -> requestApproval(action.choreId)
            is DashboardAction.ApproveChore -> approveChore(action.choreId, action.targetUserId)
            is DashboardAction.DeclineChore -> declineChore(action.choreId, action.targetUserId)
            is DashboardAction.OnChoreNameChange -> {
                updateSuccessState { it.copy(addChoreForm = it.addChoreForm.copy(nameError = null)) }
            }
            is DashboardAction.OnChorePointsChange -> {
                updateSuccessState { it.copy(addChoreForm = it.addChoreForm.copy(pointsError = null)) }
            }
            is DashboardAction.OnChoreRecurrenceChange -> {
                updateSuccessState { it.copy(addChoreForm = it.addChoreForm.copy(recurrence = action.recurrence)) }
            }
            is DashboardAction.OnChoreDueDateChange -> {
                updateSuccessState {
                    val error = if (action.date.isNotBlank() && isDateInPast(action.date, timeProvider.now())) {
                        UiText.StringResource(Res.string.due_date_past_error)
                    } else null
                    it.copy(addChoreForm = it.addChoreForm.copy(dueDate = action.date, dueDateError = error))
                }
            }
            is DashboardAction.OnChoreActiveDayToggle -> {
                updateSuccessState { 
                    val days = it.addChoreForm.activeDays.toMutableSet()
                    if (days.contains(action.dayNumber)) days.remove(action.dayNumber) else days.add(action.dayNumber)
                    it.copy(addChoreForm = it.addChoreForm.copy(activeDays = days))
                }
            }
            is DashboardAction.OnChoreWindowStartChange -> {
                updateSuccessState {
                    val form = it.addChoreForm.copy(activeWindowStart = action.time)
                    val error = validateTimeWindow(form.activeWindowStart, form.activeWindowEnd)
                    it.copy(addChoreForm = form.copy(timeWindowError = error))
                }
            }
            is DashboardAction.OnChoreWindowEndChange -> {
                updateSuccessState {
                    val form = it.addChoreForm.copy(activeWindowEnd = action.time)
                    val error = validateTimeWindow(form.activeWindowStart, form.activeWindowEnd)
                    it.copy(addChoreForm = form.copy(timeWindowError = error))
                }
            }
            is DashboardAction.OnChorePenaltyChange -> {
                updateSuccessState { it.copy(addChoreForm = it.addChoreForm.copy(penalty = action.penalty)) }
            }
            is DashboardAction.OnChoreRequiresPhotoProofToggle -> {
                updateSuccessState { it.copy(addChoreForm = it.addChoreForm.copy(requiresPhotoProof = action.required)) }
            }
            is DashboardAction.OnMemberNicknameChange -> {
                updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(nickname = action.nickname, nicknameError = null)) }
            }
            is DashboardAction.ChangeNewMemberRole -> {
                updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(role = action.role)) }
            }
            is DashboardAction.OnNewMemberPinChange -> {
                updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(pin = action.pin, pinError = null)) }
            }
            is DashboardAction.AddMember -> addMember(action.nickname, action.role, action.pin)
            is DashboardAction.UpdateUserPinRequirement -> {
                if (action.requiresPin) {
                    // Enabling PIN requirement for a child -> prompt to set a PIN first
                    val target = (_state.value as? DashboardState.Success)
                        ?.familyMembers?.find { it.id == action.userId }
                    if (target != null) {
                        updateSuccessState { 
                            it.copy(
                                pinSetupTarget = target, 
                                pinSetupDraft = "",
                                isPinSetupVisible = false,
                                pinSetupError = null, 
                                isPinChangeMode = false
                            ) 
                        }
                    }
                } else {
                    updateUserPinRequirement(action.userId, false)
                }
            }
            is DashboardAction.ChangeMemberPin -> openChangePinDialog(action.userId)
            is DashboardAction.OnPinSetupDraftChange -> {
                updateSuccessState { it.copy(pinSetupDraft = action.pin, pinSetupError = null) }
            }
            DashboardAction.TogglePinSetupVisibility -> {
                updateSuccessState { it.copy(isPinSetupVisible = !it.isPinSetupVisible) }
            }
            is DashboardAction.ConfirmChildPinSetup -> confirmChildPinSetup(
                userId = action.userId,
                pin = action.pin,
                enableRequiresPin = action.enableRequiresPin
            )
            DashboardAction.DismissChildPinSetup -> updateSuccessState {
                it.copy(
                    pinSetupTarget = null, 
                    pinSetupDraft = "",
                    isPinSetupVisible = false,
                    pinSetupError = null, 
                    isPinChangeMode = false
                )
            }
            is DashboardAction.ShowInviteQr -> showInviteQr(action.userId)
            DashboardAction.DismissInviteQr -> updateSuccessState { it.copy(inviteQrContent = null) }
            is DashboardAction.ChangeLanguage -> {
                viewModelScope.launch {
                    tokenStorage.saveLanguage(action.language.isoCode)
                }
            }
            DashboardAction.NavigateToSettings -> {
                viewModelScope.launch {
                    _events.send(DashboardEvent.NavigateToSettings)
                }
            }
            is DashboardAction.ShowPictureSourceDialog -> openPictureSourceDialog(action.userId)
            DashboardAction.DismissPictureSourceDialog -> updateSuccessState {
                it.copy(pictureTarget = null, showPresetPicker = false)
            }
            DashboardAction.ShowPresetPicker -> updateSuccessState {
                it.copy(showPresetPicker = true)
            }
            DashboardAction.DismissPresetPicker -> updateSuccessState {
                it.copy(showPresetPicker = false)
            }
            is DashboardAction.SelectPresetAvatar -> savePresetAvatar(action.userId, action.preset)
            is DashboardAction.OnPickedImage -> savePickedImage(action.userId, action.imageBytes)
            DashboardAction.StartCameraCapture -> {
                updateSuccessState { it.copy(cameraPermissionRequestCount = it.cameraPermissionRequestCount + 1) }
            }
            is DashboardAction.OnCameraPermissionResult -> {
                if (action.granted) {
                    updateSuccessState { it.copy(showCameraCapture = true, pictureTarget = it.pictureTarget) }
                }
            }
            DashboardAction.CancelCameraCapture -> {
                updateSuccessState { it.copy(showCameraCapture = false, photoProofChoreId = null) }
            }
            is DashboardAction.ShowChoreImageSourceDialog -> openChoreImageSourceDialog(action.choreId)
            DashboardAction.DismissChoreImageSourceDialog -> updateSuccessState {
                it.copy(choreImageTarget = null, showChorePresetPicker = false)
            }
            DashboardAction.ShowChorePresetPicker -> updateSuccessState {
                it.copy(showChorePresetPicker = true)
            }
            DashboardAction.DismissChorePresetPicker -> updateSuccessState {
                it.copy(showChorePresetPicker = false)
            }
            is DashboardAction.SelectChorePreset -> {
                if (action.choreId == "NEW_CHORE") {
                    val currentTemp = (_state.value as? DashboardState.Success)?.addChoreForm?.tempImagePath
                    if (currentTemp != null) {
                        viewModelScope.launch { choreRepository.clearTempChoreImage(currentTemp) }
                    }
                    val fullPreset = "bundled:" + action.preset.removePrefix("bundled:")
                    updateSuccessState {
                        it.copy(
                            addChoreForm = it.addChoreForm.copy(iconName = fullPreset, tempImagePath = null),
                            choreImageTarget = null,
                            showChorePresetPicker = false
                        )
                    }
                } else {
                    saveChorePreset(action.choreId, action.preset)
                }
            }
            is DashboardAction.OnChoreImagePicked -> {
                if (action.choreId == "NEW_CHORE") {
                    viewModelScope.launch {
                        val currentTemp = (_state.value as? DashboardState.Success)?.addChoreForm?.tempImagePath
                        if (currentTemp != null) {
                            choreRepository.clearTempChoreImage(currentTemp)
                        }
                        choreRepository.saveTempChoreImage(action.imageBytes)
                            .onSuccess { tempPath ->
                                updateSuccessState {
                                    it.copy(
                                        addChoreForm = it.addChoreForm.copy(tempImagePath = tempPath, iconName = null),
                                        choreImageTarget = null
                                    )
                                }
                            }
                            .onFailure {
                                updateSuccessState { it.copy(choreImageTarget = null) }
                            }
                    }
                } else {
                    saveChoreImage(action.choreId, action.imageBytes)
                }
            }
            is DashboardAction.ClearChoreImage -> {
                if (action.choreId == "NEW_CHORE") {
                    val currentTemp = (_state.value as? DashboardState.Success)?.addChoreForm?.tempImagePath
                    if (currentTemp != null) {
                        viewModelScope.launch { choreRepository.clearTempChoreImage(currentTemp) }
                    }
                    updateSuccessState {
                        it.copy(
                            addChoreForm = it.addChoreForm.copy(iconName = null, tempImagePath = null),
                            choreImageTarget = null,
                            showChorePresetPicker = false
                        )
                    }
                } else {
                    saveChorePreset(action.choreId, "")
                }
            }
            is DashboardAction.StartPhotoProofCapture -> {
                updateSuccessState { 
                    it.copy(
                        cameraPermissionRequestCount = it.cameraPermissionRequestCount + 1,
                        photoProofChoreId = action.choreId 
                    )
                }
            }
            is DashboardAction.OnPhotoProofCaptured -> {
                uploadPhotoProof(action.choreId, action.imageBytes)
            }
        }
    }

    private fun uploadPhotoProof(choreId: String, imageBytes: ByteArray) {
        val currentState = _state.value as? DashboardState.Success ?: return
        viewModelScope.launch {
            updateSuccessState { it.copy(isSavingPicture = true, showCameraCapture = false) }
            choreRepository.uploadPhotoProof(
                familyId = currentState.user.familyId,
                choreId = choreId,
                fileName = "proof_${choreId}_${timeProvider.now()}.jpg",
                photoBytes = imageBytes
            ).onSuccess {
                updateSuccessState { it.copy(isSavingPicture = false, photoProofChoreId = null) }
                requestApproval(choreId) // Auto-submit after proof is captured
            }.onFailure { error ->
                logger.e { "Failed to upload photo proof: $error" }
                updateSuccessState { it.copy(isSavingPicture = false, photoProofChoreId = null) }
            }
        }
    }

    private fun openPictureSourceDialog(userId: String) {
        val target = (_state.value as? DashboardState.Success)
            ?.familyMembers?.find { it.id == userId }
        if (target != null) {
            updateSuccessState { it.copy(pictureTarget = target, showPresetPicker = false, isSavingPicture = false) }
        }
    }

    private fun savePresetAvatar(userId: String, preset: String) {
        viewModelScope.launch {
            updateSuccessState { it.copy(isSavingPicture = true) }
            profilePictureRepository.savePreset(userId, preset)
                .onSuccess {
                    updateSuccessState {
                        it.copy(
                            pictureTarget = null,
                            showPresetPicker = false,
                            isSavingPicture = false,
                        )
                    }
                    _events.send(DashboardEvent.ShowMessage(UiText.StringResource(Res.string.picture_updated_success)))
                    loadMemberPictures()
                }
                .onFailure { e ->
                    logger.e { "Failed to save preset avatar for $userId: $e" }
                    updateSuccessState { it.copy(isSavingPicture = false) }
                }
        }
    }

    private fun savePickedImage(userId: String, imageBytes: ByteArray) {
        viewModelScope.launch {
            updateSuccessState { it.copy(isSavingPicture = true, showCameraCapture = false) }
            profilePictureRepository.saveCustomPicture(userId, imageBytes)
                .onSuccess {
                    updateSuccessState {
                        it.copy(
                            pictureTarget = null,
                            showPresetPicker = false,
                            isSavingPicture = false,
                        )
                    }
                    _events.send(DashboardEvent.ShowMessage(UiText.StringResource(Res.string.picture_updated_success)))
                    loadMemberPictures()
                }
                .onFailure { e ->
                    logger.e { "Failed to save picked image for $userId: $e" }
                    updateSuccessState { it.copy(isSavingPicture = false) }
                }
        }
    }

    private fun loadMemberPictures() {
        val members = (_state.value as? DashboardState.Success)?.familyMembers ?: return
        viewModelScope.launch {
            val pictures = mutableMapOf<String, String>()
            members.forEach { member ->
                profilePictureRepository.getProfilePicture(member.id)?.let { path ->
                    pictures[member.id] = path
                }
            }
            updateSuccessState { it.copy(memberPictures = pictures) }
        }
    }

    private fun openChoreImageSourceDialog(choreId: String) {
        val exists = (_state.value as? DashboardState.Success)?.chores?.any { it.id == choreId } ?: false
        if (exists) {
            updateSuccessState { it.copy(choreImageTarget = choreId, showChorePresetPicker = false, isSavingChoreImage = false) }
        }
    }

    private fun saveChorePreset(choreId: String, preset: String) {
        val familyId = (_state.value as? DashboardState.Success)?.user?.familyId ?: return
        val cleanPreset = preset.removePrefix("bundled:")
        viewModelScope.launch {
            updateSuccessState { it.copy(isSavingChoreImage = true) }
            choreRepository.saveChoreImagePreset(familyId, choreId, cleanPreset)
                .onSuccess {
                    updateSuccessState {
                        it.copy(
                            choreImageTarget = null,
                            showChorePresetPicker = false,
                            isSavingChoreImage = false,
                        )
                    }
                    _events.send(DashboardEvent.ShowMessage(UiText.StringResource(Res.string.chore_image_updated_success)))
                }
                .onFailure { e ->
                    logger.e { "Failed to save preset image for chore $choreId: $e" }
                    updateSuccessState { it.copy(isSavingChoreImage = false) }
                }
        }
    }

    private fun saveChoreImage(choreId: String, imageBytes: ByteArray) {
        val familyId = (_state.value as? DashboardState.Success)?.user?.familyId ?: return
        viewModelScope.launch {
            updateSuccessState { it.copy(isSavingChoreImage = true) }
            choreRepository.saveChoreImage(familyId, choreId, imageBytes)
                .onSuccess {
                    updateSuccessState {
                        it.copy(
                            choreImageTarget = null,
                            showChorePresetPicker = false,
                            isSavingChoreImage = false,
                        )
                    }
                    _events.send(DashboardEvent.ShowMessage(UiText.StringResource(Res.string.chore_image_updated_success)))
                }
                .onFailure { e ->
                    logger.e { "Failed to save chore image for chore $choreId: $e" }
                    updateSuccessState { it.copy(isSavingChoreImage = false) }
                }
        }
    }

    private fun addMember(nickname: String, role: UserRole, pin: String?) {
        val currentState = _state.value as? DashboardState.Success ?: return
        if (!currentState.isServerReachable && !currentState.isOfflineMode) return

        val error = AuthValidator.validateNickname(nickname)
        if (error != null) {
            updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(nicknameError = error.toUiText())) }
            return
        }

        // For Parent, PIN is mandatory and must be valid
        if (role == UserRole.PARENT && pin.isNullOrBlank()) {
            updateSuccessState {
                it.copy(addMemberForm = it.addMemberForm.copy(pinError = AuthValidator.validatePin("")?.toUiText()))
            }
            return
        }

        if (!pin.isNullOrBlank()) {
            val pinError = AuthValidator.validatePin(pin)
            if (pinError != null) {
                updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(pinError = pinError.toUiText())) }
                return
            }
        }

        val requiresPin = role == UserRole.PARENT || !pin.isNullOrBlank()

        viewModelScope.launch {
            updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(isAdding = true)) }
            authRepository.addFamilyMember(currentState.user.familyId, nickname, role, pin, requiresPin)
                .onSuccess { newUser ->
                    updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(isAdding = false, nickname = "", pin = "", pinError = null, nicknameError = null)) }
                    loadDashboardData(isRefreshing = true) // Refresh members
                    if (!currentState.isOfflineMode) {
                        showInviteQr(newUser.id)
                    }
                }
                .onFailure { e ->
                    logger.e { "Failed to add member: $e" }
                    updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(isAdding = false)) }
                }
        }
    }

    private fun updateUserPinRequirement(userId: String, requiresPin: Boolean) {
        viewModelScope.launch {
            authRepository.updateUserPinRequirement(userId, requiresPin)
                .onSuccess {
                    loadDashboardData(isRefreshing = true)
                }
                .onFailure { e ->
                    logger.e { "Failed to update PIN requirement: $e" }
                }
        }
    }

    private fun openChangePinDialog(userId: String) {
        val target = (_state.value as? DashboardState.Success)
            ?.familyMembers?.find { it.id == userId }
        if (target != null) {
            updateSuccessState { 
                it.copy(
                    pinSetupTarget = target, 
                    pinSetupDraft = "",
                    isPinSetupVisible = false,
                    pinSetupError = null, 
                    isPinChangeMode = true
                ) 
            }
        }
    }

    private fun confirmChildPinSetup(userId: String, pin: String, enableRequiresPin: Boolean) {
        val validationError = AuthValidator.validatePin(pin)
        if (validationError != null) {
            updateSuccessState { it.copy(pinSetupError = validationError.toUiText()) }
            return
        }

        viewModelScope.launch {
            updateSuccessState { it.copy(pinSetupError = null, pinSetupSaving = true) }
            authRepository.setupPin(userId, pin)
                .onSuccess {
                    val currentState = _state.value as? DashboardState.Success
                    val isOwnPin = currentState?.user?.id == userId

                    if (enableRequiresPin) {
                        authRepository.updateUserPinRequirement(userId, true)
                            .onSuccess {
                                updateSuccessState {
                                    it.copy(
                                        pinSetupTarget = null,
                                        pinSetupDraft = "",
                                        isPinSetupVisible = false,
                                        pinSetupError = null,
                                        pinSetupSaving = false,
                                        isPinChangeMode = false
                                    )
                                }
                                _events.send(DashboardEvent.ShowMessage(UiText.StringResource(Res.string.child_pin_set_success)))
                                loadDashboardData(isRefreshing = true)
                            }
                            .onFailure { e ->
                                logger.e { "Failed to enable PIN requirement for $userId: $e" }
                                updateSuccessState { it.copy(pinSetupError = e.toUiText(), pinSetupSaving = false) }
                            }
                    } else {
                        // Direct PIN override (admin reset / change). Does not touch requiresPin.
                        updateSuccessState {
                            it.copy(
                                pinSetupTarget = null,
                                pinSetupDraft = "",
                                isPinSetupVisible = false,
                                pinSetupError = null,
                                pinSetupSaving = false,
                                isPinChangeMode = false
                            )
                        }
                        loadDashboardData(isRefreshing = true)
                        if (isOwnPin) {
                            // Security: force re-auth with the new PIN via User Selection
                            _events.send(
                                DashboardEvent.Logout(
                                    isServerOnline = currentState.isServerReachable,
                                    familyId = currentState.user.familyId
                                )
                            )
                        } else {
                            _events.send(DashboardEvent.ShowMessage(UiText.StringResource(Res.string.member_pin_change_success)))
                        }
                    }
                }
                .onFailure { e ->
                    logger.e { "Failed to set PIN for $userId: $e" }
                    updateSuccessState { it.copy(pinSetupError = e.toUiText(), pinSetupSaving = false) }
                }
        }
    }

    private fun showInviteQr(userId: String?) {
        val currentState = _state.value as? DashboardState.Success ?: return
        if (currentState.isOfflineMode || !currentState.isServerReachable) return
        
        viewModelScope.launch {
            authRepository.generatePairingToken(currentState.user.familyId)
                .onSuccess { token ->
                    val serverUrl = tokenStorage.getServerUrl() ?: ""
                    val pairingToken = PairingToken(
                        token = token,
                        serverIp = serverUrl,
                        familyId = currentState.user.familyId,
                        userId = userId,
                        familyName = "" // Set in pairing token generation on server or handled by UI
                    )
                    val json = Json.encodeToString(pairingToken)
                    updateSuccessState { it.copy(inviteQrContent = json) }
                }
                .onFailure { e ->
                    logger.e { "Failed to generate pairing token: $e" }
                }
        }
    }

    private fun validateTimeWindow(startStr: String, endStr: String): UiText? {
        val startMin = startStr.toMinutesOfDay()
        val endMin = endStr.toMinutesOfDay()
        if (startMin != null && endMin != null && endMin <= startMin) {
            return UiText.StringResource(Res.string.time_window_invalid_error)
        }
        return null
    }

    private fun createChore(action: DashboardAction.CreateChore) {
        val currentState = _state.value as? DashboardState.Success ?: return
        val form = currentState.addChoreForm
        val now = timeProvider.now()
        
        val nameError = ChoreValidator.validateName(action.name)
        val pointsError = ChoreValidator.validatePoints(action.points)
        val assigneeError = ChoreValidator.validateAssignee(action.assignedTo)
        val timeWindowError = validateTimeWindow(form.activeWindowStart, form.activeWindowEnd)
        val dueDateError = if (form.dueDate.isNotBlank() && isDateInPast(form.dueDate, now)) {
            UiText.StringResource(Res.string.due_date_past_error)
        } else null
        
        if (nameError != null || pointsError != null || assigneeError != null || timeWindowError != null || dueDateError != null) {
            updateSuccessState {
                it.copy(
                    addChoreForm = it.addChoreForm.copy(
                        nameError = nameError?.toUiText(),
                        pointsError = pointsError?.toUiText(),
                        timeWindowError = timeWindowError,
                        dueDateError = dueDateError
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            val now = timeProvider.now()
            val form = currentState.addChoreForm
            val dueDateMillis = form.dueDate.takeIf { it.isNotBlank() }?.let { dateStr ->
                parseDateToEpochMillis(dateStr)
            }
            val selectedIcon = form.iconName ?: form.tempImagePath
            val chore = Chore(
                id = "chore_${action.assignedTo}_$now",
                familyId = currentState.user.familyId,
                name = action.name.trim(),
                description = action.description?.trim(),
                points = action.points,
                status = ChoreStatus.PENDING,
                assignedTo = action.assignedTo,
                createdBy = currentState.user.id,
                createdAt = now,
                updatedAt = now,
                recurrence = action.recurrence,
                activeDays = action.activeDays,
                activeWindowStart = form.activeWindowStart.toMinutesOfDay(),
                activeWindowEnd = form.activeWindowEnd.toMinutesOfDay(),
                penalty = action.penalty,
                requiresPhotoProof = action.requiresPhotoProof,
                iconName = selectedIcon,
                parentChoreId = null,
                dueDate = dueDateMillis
            )
            // Clear form and errors
            updateSuccessState {
                it.copy(
                    addChoreForm = AddChoreFormState()
                )
            }
            
            choreRepository.createChore(chore)
                .onSuccess {
                    if (form.tempImagePath != null) {
                        saveChoreImageFromPath(currentState.user.familyId, chore.id, form.tempImagePath)
                    }
                }
                .onFailure { error ->
                    logger.e { "Failed to create chore: $error" }
                }
        }
    }

    private fun saveChoreImageFromPath(familyId: String, choreId: String, filePath: String) {
        viewModelScope.launch {
            updateSuccessState { it.copy(isSavingChoreImage = true) }
            choreRepository.saveChoreImageFromPath(familyId, choreId, filePath)
                .onSuccess {
                    updateSuccessState { it.copy(isSavingChoreImage = false) }
                    loadDashboardData(isRefreshing = true)
                }
                .onFailure { e ->
                    logger.e { "Failed to upload image from path for chore $choreId: $e" }
                    updateSuccessState { it.copy(isSavingChoreImage = false) }
                }
        }
    }

    private fun requestApproval(choreId: String) {
        val currentState = _state.value as? DashboardState.Success ?: return
        viewModelScope.launch {
            choreRepository.updateChoreStatus(
                familyId = currentState.user.familyId,
                choreId = choreId,
                newStatus = ChoreStatus.PENDING_APPROVAL,
                adminId = currentState.user.id
            ).onSuccess {
                triggerSyncFallback(currentState.user.familyId)
            }.onFailure { error ->
                logger.e { "Failed to request approval: $error" }
            }
        }
    }

    private fun approveChore(choreId: String, targetUserId: String) {
        val currentState = _state.value as? DashboardState.Success ?: return
        viewModelScope.launch {
            choreRepository.updateChoreStatus(
                familyId = currentState.user.familyId,
                choreId = choreId,
                newStatus = ChoreStatus.APPROVED,
                adminId = currentState.user.id
            ).onSuccess {
                triggerSyncFallback(currentState.user.familyId)
                val chore = currentState.chores.find { it.id == choreId }
                if (chore != null && chore.points > 0) {
                    val now = timeProvider.now()
                    val transaction = Transaction(
                        id = "tr_${targetUserId}_${choreId}_$now",
                        familyId = currentState.user.familyId,
                        userId = targetUserId,
                        adminId = currentState.user.id,
                        amount = chore.points,
                        type = TransactionType.BONUS,
                        timestamp = now,
                        note = "Chore Approved: ${chore.name}"
                    )
                    transactionRepository.addTransaction(transaction)
                }
            }.onFailure { error ->
                logger.e { "Failed to approve chore: $error" }
            }
        }
    }

    private fun declineChore(choreId: String, targetUserId: String) {
        val currentState = _state.value as? DashboardState.Success ?: return
        viewModelScope.launch {
            val chore = currentState.chores.find { it.id == choreId } ?: return@launch
            
            // Check time window. If passed, REJECTED. Else PENDING.
            val now = timeProvider.now()
            // Simplified check: if it has an activeWindowEnd, we'd check it. For now, we revert to PENDING.
            // A more complex check requires mapping now to minutes from midnight.
            val isWindowPassed = false // TODO: implement precise window check
            
            val newStatus = if (isWindowPassed) ChoreStatus.REJECTED else ChoreStatus.PENDING
            
            choreRepository.updateChoreStatus(
                familyId = currentState.user.familyId,
                choreId = choreId,
                newStatus = newStatus,
                adminId = currentState.user.id
            ).onSuccess {
                triggerSyncFallback(currentState.user.familyId)
                if (newStatus == ChoreStatus.REJECTED && chore.penalty > 0) {
                    val transaction = Transaction(
                        id = "tr_${targetUserId}_${choreId}_$now",
                        familyId = currentState.user.familyId,
                        userId = targetUserId,
                        adminId = currentState.user.id,
                        amount = -chore.penalty,
                        type = TransactionType.PENALTY,
                        timestamp = now,
                        note = "Chore Missed/Declined: ${chore.name}"
                    )
                    transactionRepository.addTransaction(transaction)
                }
            }.onFailure { error ->
                logger.e { "Failed to decline chore: $error" }
            }
        }
    }

    private suspend fun triggerSyncFallback(familyId: String) {
        val currentState = _state.value as? DashboardState.Success ?: return
        if (currentState.isServerReachable && !currentState.isOfflineMode) {
            choreRepository.syncChores(familyId).onFailure { error ->
                logger.e { "Manual sync fallback failed: $error" }
            }
        }
    }

    private fun awardPoints(targetUserId: String, item: BehaviorItem) {
        val currentState = _state.value as? DashboardState.Success ?: return
        viewModelScope.launch {
            val now = timeProvider.now()
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

    private var observationsJob: kotlinx.coroutines.Job? = null
    private var loadJob: kotlinx.coroutines.Job? = null

    private fun loadDashboardData(isRefreshing: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (isRefreshing) {
                updateSuccessState { it.copy(isRefreshing = true) }
            } else {
                _state.value = DashboardState.Loading
            }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    // Load family members
                    val membersResult = authRepository.getFamilyMembers(user.familyId)
                    val familyMembers = membersResult.getOrElse { emptyList<User>() }
                    val langCode = tokenStorage.getLanguage()
                    val currentLang = AppLanguage.entries.find { it.isoCode == langCode } ?: AppLanguage.ENGLISH
                    val isOffline = tokenStorage.getOfflineMode() ?: false

                    val previousSuccessState = _state.value as? DashboardState.Success
                    val currentTab = previousSuccessState?.currentTab
                        ?: if (user.role == UserRole.PARENT) ParentOverviewRoute else ChildTodayRoute
                    val currentAssigneeId = previousSuccessState?.selectedAssigneeId
                        ?: familyMembers.firstOrNull { it.role == UserRole.CHILD }?.id 
                        ?: familyMembers.firstOrNull()?.id

                    // Initial state setup
                    _state.value = DashboardState.Success(
                        user = user,
                        language = currentLang,
                        familyMembers = familyMembers,
                        selectedAssigneeId = currentAssigneeId,
                        transactions = previousSuccessState?.transactions ?: emptyList(),
                        chores = previousSuccessState?.chores ?: emptyList(),
                        behaviorItems = if (user.role == UserRole.PARENT) BehaviorDefaults.defaultItems else emptyList(),
                        currentTab = currentTab,
                        isOfflineMode = isOffline,
                        isRefreshing = false,
                        isCameraAvailable = hardwareAvailabilityRepository.hasCamera(),
                        addChoreForm = previousSuccessState?.addChoreForm ?: AddChoreFormState(),
                        addMemberForm = previousSuccessState?.addMemberForm ?: AddMemberFormState(),
                        inviteQrContent = previousSuccessState?.inviteQrContent,
                        pinSetupTarget = previousSuccessState?.pinSetupTarget,
                        pinSetupError = previousSuccessState?.pinSetupError,
                        pinSetupSaving = previousSuccessState?.pinSetupSaving ?: false,
                        memberPictures = previousSuccessState?.memberPictures ?: emptyMap(),
                        choreImages = previousSuccessState?.choreImages ?: emptyMap()
                    )

                    // Refresh local profile pictures (local-only, not on the server model)
                    loadMemberPictures()

                    // Restart observations for the new user
                    observationsJob?.cancel()
                    observationsJob = viewModelScope.launch {
                        // Generate instances for today/tomorrow before listening
                        choreGenerationUseCase(user.familyId)

                        val transactionFlow = if (user.role == UserRole.PARENT) {
                            transactionRepository.getTransactionsForFamily(user.familyId)
                        } else {
                            transactionRepository.getTransactionsForUser(user.id)
                        }

                        transactionFlow
                            .onEach { transactions ->
                                updateSuccessState { currentState ->
                                    val updatedMembers = currentState.familyMembers.map { member ->
                                        val memberPoints = transactions
                                            .filter { it.userId == member.id }
                                            .sumOf { it.amount }
                                        member.copy(points = memberPoints)
                                    }
                                    val currentUserPoints = transactions
                                        .filter { it.userId == currentState.user.id }
                                        .sumOf { it.amount }
                                    val updatedUser = currentState.user.copy(points = currentUserPoints)

                                    currentState.copy(
                                        transactions = transactions,
                                        familyMembers = updatedMembers,
                                        user = updatedUser
                                    )
                                }
                            }
                            .launchIn(this)

                        val choreFlow = if (user.role == UserRole.PARENT) {
                            choreRepository.getChoresForFamily(user.familyId)
                        } else {
                            choreRepository.getChoresForUser(user.id)
                        }

                        choreFlow
                            .onEach { rawChores ->
                                val now = timeProvider.now()
                                val filteredChores = filterChoresForUser(rawChores, user, now)
                                val counts = mutableMapOf<String, Int>()

                                if (user.role == UserRole.PARENT) {
                                    val pendingApproval = rawChores.count { it.status == ChoreStatus.PENDING_APPROVAL }
                                    if (pendingApproval > 0) counts["tasks"] = pendingApproval
                                    
                                    familyMembers.filter { it.role == UserRole.CHILD }.forEach { child ->
                                        val childPending = rawChores.count { it.assignedTo == child.id && it.status == ChoreStatus.PENDING_APPROVAL }
                                        if (childPending > 0) counts[child.id] = childPending
                                    }
                                } else {
                                    val myPending = filteredChores.count { it.assignedTo == user.id && (it.status == ChoreStatus.PENDING || it.status == ChoreStatus.IN_PROGRESS) }
                                    if (myPending > 0) counts["today"] = myPending
                                }
                                
                                val choreImages = filteredChores.mapNotNull { chore -> chore.iconName?.let { chore.id to it } }.toMap()
                                updateSuccessState { it.copy(chores = filteredChores, pendingCounts = counts, choreImages = choreImages) }
                            }
                            .launchIn(this)
                            
                        if (!isOffline) { 
                            // Start listening for websocket updates
                            choreRepository.observeRemoteChanges(user.familyId)
                                .onEach {
                                    // When a change is detected via websocket, the remote data source will upsert the local database.
                                    // Room will emit a new list on getChoresForUser, so we just need to collect this to keep it active.
                                    logger.d { "Received remote chore change" }
                                }
                                .launchIn(this)
                        }
                    }
                }
                .onFailure { error ->
                    logger.e { "Failed to load dashboard data: $error" }
                    _state.value = DashboardState.Error(
                        UiText.StringResource(Res.string.dashboard_load_failed_error)
                    )
                }
        }
    }

    private fun filterChoresForUser(chores: List<Chore>, user: User, nowMillis: Long): List<Chore> {
        val todayStart = startOfTodayEpochMillis(nowMillis)
        val tomorrowStart = todayStart + 86_400_000L

        return if (user.role == UserRole.CHILD) {
            chores.filter { chore ->
                if (chore.parentChoreId == null) {
                    if (chore.recurrence == Recurrence.NONE || chore.recurrence == Recurrence.CUSTOM) {
                        val due = chore.dueDate
                        due == null || due < tomorrowStart
                    } else {
                        false
                    }
                } else {
                    val due = chore.dueDate ?: 0L
                    due >= todayStart && due < tomorrowStart
                }
            }
        } else {
            chores.filter { chore ->
                if (chore.parentChoreId == null) {
                    true
                } else {
                    chore.status == ChoreStatus.PENDING_APPROVAL
                }
            }
        }
    }

    private fun updateSuccessState(update: (DashboardState.Success) -> DashboardState.Success) {
        _state.update { currentState ->
            if (currentState is DashboardState.Success) {
                update(currentState)
            } else {
                currentState
            }
        }
    }

    private fun observeConnectivity() {
        observeConnectivityUseCase()
            .onEach { status ->
                updateSuccessState {
                    it.copy(
                        isOfflineMode = status.isOfflineMode,
                        isServerReachable = status.isServerReachable,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLanguage() {
        tokenStorage.language
            .onEach { languageCode ->
                val newLang = AppLanguage.entries.find { it.isoCode == languageCode } ?: AppLanguage.ENGLISH
                updateSuccessState { it.copy(language = newLang) }
            }
            .launchIn(viewModelScope)
    }
}
