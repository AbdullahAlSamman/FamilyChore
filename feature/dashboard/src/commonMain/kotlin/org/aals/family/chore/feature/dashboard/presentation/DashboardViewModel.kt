package org.aals.family.chore.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.dashboard_load_failed_error
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.repository.TransactionRepository
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
}

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val choreRepository: ChoreRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val tokenStorage: TokenStorage,
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
                updateSuccessState { it.copy(choreNameError = null) }
            }
            is DashboardAction.OnChorePointsChange -> {
                updateSuccessState { it.copy(chorePointsError = null) }
            }
            is DashboardAction.OnChildNicknameChange -> {
                updateSuccessState { it.copy(newChildNickname = action.nickname, childNicknameError = null) }
            }
            DashboardAction.TogglePinRequirement -> {
                updateSuccessState { it.copy(requiresPinForNewChild = !it.requiresPinForNewChild) }
            }
            is DashboardAction.AddChild -> addChild(action.nickname, action.requiresPin)
            is DashboardAction.UpdateUserPinRequirement -> updateUserPinRequirement(action.userId, action.requiresPin)
            is DashboardAction.ShowInviteQr -> showInviteQr(action.userId)
            DashboardAction.DismissInviteQr -> updateSuccessState { it.copy(inviteQrContent = null) }
            is DashboardAction.ChangeLanguage -> {
                viewModelScope.launch {
                    tokenStorage.saveLanguage(action.language.isoCode)
                }
            }
        }
    }

    private fun addChild(nickname: String, requiresPin: Boolean) {
        val currentState = _state.value as? DashboardState.Success ?: return
        if (!currentState.isServerReachable) return

        val error = AuthValidator.validateNickname(nickname)
        if (error != null) {
            updateSuccessState { it.copy(childNicknameError = error.toUiText()) }
            return
        }

        viewModelScope.launch {
            updateSuccessState { it.copy(isAddingChild = true) }
            authRepository.addChildUser(currentState.user.familyId, nickname, requiresPin)
                .onSuccess { newUser ->
                    updateSuccessState { it.copy(isAddingChild = false, newChildNickname = "") }
                    loadDashboardData(isRefreshing = true) // Refresh members
                    showInviteQr(newUser.id)
                }
                .onFailure { e ->
                    logger.e { "Failed to add child: $e" }
                    updateSuccessState { it.copy(isAddingChild = false) }
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

    private fun showInviteQr(userId: String?) {
        val currentState = _state.value as? DashboardState.Success ?: return
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
                    choreNameError = nameError?.toUiText(),
                    chorePointsError = pointsError?.toUiText()
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
            updateSuccessState { it.copy(choreNameError = null, chorePointsError = null) }
            
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

                    // Initial state setup
                    _state.value = DashboardState.Success(
                        user = user,
                        language = currentLang,
                        familyMembers = familyMembers,
                        selectedAssigneeId = familyMembers.firstOrNull { it.role == UserRole.CHILD }?.id 
                            ?: familyMembers.firstOrNull()?.id,
                        transactions = emptyList(),
                        chores = emptyList(),
                        behaviorItems = if (user.role == UserRole.PARENT) BehaviorDefaults.defaultItems else emptyList(),
                        currentTab = if (user.role == UserRole.PARENT) ParentOverviewRoute else ChildTodayRoute,
                        isRefreshing = false
                    )

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
        connectivityRepository.isServerReachable
            .onEach { isReachable ->
                updateSuccessState { it.copy(isServerReachable = isReachable) }
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
