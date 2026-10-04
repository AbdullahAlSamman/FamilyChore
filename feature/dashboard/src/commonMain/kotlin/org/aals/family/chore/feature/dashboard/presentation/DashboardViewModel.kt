package org.aals.family.chore.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.child_pin_set_success
import familychore.core.generated.resources.dashboard_load_failed_error
import familychore.core.generated.resources.member_pin_change_success
import familychore.core.generated.resources.picture_updated_success
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
import org.aals.family.chore.core.domain.usecase.ObserveConnectivityUseCase
import org.aals.family.chore.core.domain.util.TimeProvider
import org.aals.family.chore.core.domain.util.getOrElse
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess
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
            is DashboardAction.OnChoreNameChange -> {
                updateSuccessState { it.copy(addChoreForm = it.addChoreForm.copy(nameError = null)) }
            }
            is DashboardAction.OnChorePointsChange -> {
                updateSuccessState { it.copy(addChoreForm = it.addChoreForm.copy(pointsError = null)) }
            }
            is DashboardAction.OnMemberNicknameChange -> {
                updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(nickname = action.nickname, nicknameError = null)) }
            }
            is DashboardAction.ChangeNewMemberRole -> {
                updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(role = action.role)) }
            }
            is DashboardAction.OnNewMemberPinChange -> {
                updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(pin = action.pin)) }
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
                updateSuccessState { it.copy(showCameraCapture = false) }
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

    private fun addMember(nickname: String, role: UserRole, pin: String?) {
        val currentState = _state.value as? DashboardState.Success ?: return
        if (!currentState.isServerReachable && !currentState.isOfflineMode) return

        val error = AuthValidator.validateNickname(nickname)
        if (error != null) {
            updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(nicknameError = error.toUiText())) }
            return
        }

        // For Parent, PIN is mandatory
        if (role == UserRole.PARENT && pin.isNullOrBlank()) {
            // Should show PIN error, but for now just log
            logger.e { "Parent requires a PIN" }
            return
        }

        val requiresPin = role == UserRole.PARENT || !pin.isNullOrBlank()

        viewModelScope.launch {
            updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(isAdding = true)) }
            authRepository.addFamilyMember(currentState.user.familyId, nickname, role, pin, requiresPin)
                .onSuccess { newUser ->
                    updateSuccessState { it.copy(addMemberForm = it.addMemberForm.copy(isAdding = false, nickname = "", pin = "")) }
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

    private fun createChore(action: DashboardAction.CreateChore) {
        val currentState = _state.value as? DashboardState.Success ?: return
        
        val nameError = ChoreValidator.validateName(action.name)
        val pointsError = ChoreValidator.validatePoints(action.points)
        val assigneeError = ChoreValidator.validateAssignee(action.assignedTo)
        
        if (nameError != null || pointsError != null || assigneeError != null) {
            updateSuccessState {
                it.copy(
                    addChoreForm = it.addChoreForm.copy(
                        nameError = nameError?.toUiText(),
                        pointsError = pointsError?.toUiText(),
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            val now = timeProvider.now()
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
                updatedAt = now
            )
            // Clear errors before attempting to save
            updateSuccessState {
                it.copy(
                    addChoreForm = it.addChoreForm.copy(
                        nameError = null,
                        pointsError = null,
                    )
                )
            }
            
            choreRepository.createChore(chore)
                .onFailure { error ->
                    logger.e { "Failed to create chore: $error" }
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
                        memberPictures = previousSuccessState?.memberPictures ?: emptyMap()
                    )

                    // Refresh local profile pictures (local-only, not on the server model)
                    loadMemberPictures()

                    // Restart observations for the new user
                    observationsJob?.cancel()
                    observationsJob = viewModelScope.launch {
                        transactionRepository.getTransactionsForUser(user.id)
                            .onEach { transactions ->
                                updateSuccessState { it.copy(transactions = transactions) }
                            }
                            .launchIn(this)

                        choreRepository.getChoresForUser(user.id)
                            .onEach { chores ->
                                updateSuccessState { it.copy(chores = chores) }
                            }
                            .launchIn(this)
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
