package org.aals.family.chore.feature.auth.presentation.user_selection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.error_missing_params
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.usecase.ConnectivityStatus
import org.aals.family.chore.core.domain.usecase.ObserveConnectivityUseCase
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess
import org.aals.family.chore.core.presentation.UiText
import org.aals.family.chore.core.presentation.toUiText

class UserSelectionViewModel(
    private val authRepository: AuthRepository,
    private val observeConnectivityUseCase: ObserveConnectivityUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val logger: Logger,
) : ViewModel() {

    private val pairingToken: String? = savedStateHandle["pairingToken"]
    private val familyId: String? = savedStateHandle["familyId"]
    private val isFirstTimeOnboarding: Boolean = savedStateHandle["isFirstTimeOnboarding"] ?: false

    private val _state = MutableStateFlow<UserSelectionState>(UserSelectionState.Loading())
    val state = _state.asStateFlow()

    private val _events = Channel<UserSelectionEvent>()
    val events = _events.receiveAsFlow()

    private var currentConnectivityStatus = ConnectivityStatus()

    init {
        observeConnectivity()
        loadUsers()
    }

    private fun observeConnectivity() {
        observeConnectivityUseCase()
            .onEach { status ->
                currentConnectivityStatus = status
                _state.update { currentState ->
                    when (currentState) {
                        is UserSelectionState.Loading -> currentState.copy(
                            isOfflineMode = status.isOfflineMode,
                            isServerReachable = status.isServerReachable,
                        )
                        is UserSelectionState.Success -> currentState.copy(
                            isOfflineMode = status.isOfflineMode,
                            isServerReachable = status.isServerReachable,
                        )
                        is UserSelectionState.Error -> currentState.copy(
                            isOfflineMode = status.isOfflineMode,
                            isServerReachable = status.isServerReachable,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: UserSelectionAction) {
        when (action) {
            is UserSelectionAction.OnUserClick -> {
                logger.d { "User selected: ${action.user.nickname} (${action.user.id})" }
                if (pairingToken != null) {
                    confirmPairing(action.user.id)
                } else {
                    if (action.user.role == UserRole.CHILD && !action.user.requiresPin) {
                        logger.d { "PIN not required for child, bypassing" }
                        viewModelScope.launch {
                            authRepository.selectUser(action.user.id)
                            _events.send(UserSelectionEvent.PinVerified)
                        }
                    } else {
                        viewModelScope.launch {
                            _events.send(UserSelectionEvent.PairingConfirmed(action.user.id))
                        }
                    }
                }
            }
            UserSelectionAction.OnRetryClick -> {
                loadUsers()
            }
        }
    }

    private fun loadUsers() {
        logger.d { "Loading users for selection (token: ${pairingToken != null}, familyId: $familyId)" }
        viewModelScope.launch {
            _state.value = UserSelectionState.Loading(
                isOfflineMode = currentConnectivityStatus.isOfflineMode,
                isServerReachable = currentConnectivityStatus.isServerReachable,
            )
            val result = when {
                pairingToken != null -> authRepository.getPairingUsers(pairingToken)
                familyId != null -> authRepository.getFamilyMembers(familyId)
                else -> {
                    _state.value = UserSelectionState.Error(
                        message = UiText.StringResource(Res.string.error_missing_params),
                        isOfflineMode = currentConnectivityStatus.isOfflineMode,
                        isServerReachable = currentConnectivityStatus.isServerReachable,
                    )
                    return@launch
                }
            }

            result
                .onSuccess { users ->
                    _state.value = UserSelectionState.Success(
                        users = users,
                        isFromDiscovery = isFirstTimeOnboarding || pairingToken != null,
                        isOfflineMode = currentConnectivityStatus.isOfflineMode,
                        isServerReachable = currentConnectivityStatus.isServerReachable,
                    )
                }
                .onFailure { error ->
                    _state.value = UserSelectionState.Error(
                        message = error.toUiText(),
                        isOfflineMode = currentConnectivityStatus.isOfflineMode,
                        isServerReachable = currentConnectivityStatus.isServerReachable,
                    )
                }
        }
    }

    private fun confirmPairing(userId: String) {
        val currentToken = pairingToken ?: return
        val currentSuccess = _state.value as? UserSelectionState.Success ?: return

        viewModelScope.launch {
            _state.value = currentSuccess.copy(isConfirming = true, error = null)
            authRepository.confirmPairing(currentToken, userId)
                .onSuccess { user ->
                    _state.value = currentSuccess.copy(isConfirming = false)
                    if (user.role == UserRole.CHILD && !user.requiresPin) {
                        logger.d { "PIN not required for paired child, bypassing" }
                        authRepository.selectUser(user.id)
                        _events.send(UserSelectionEvent.PinVerified)
                    } else {
                        _events.send(UserSelectionEvent.PairingConfirmed(user.id))
                    }
                }
                .onFailure { error ->
                    _state.value = currentSuccess.copy(isConfirming = false, error = error.toUiText())
                }
        }
    }
}
