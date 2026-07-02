package org.aals.family.chore.feature.auth.presentation.user_selection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess

class UserSelectionViewModel(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
    private val logger: Logger
) : ViewModel() {

    private val pairingToken: String? = savedStateHandle["pairingToken"]
    private val familyId: String? = savedStateHandle["familyId"]

    private val _state = MutableStateFlow<UserSelectionState>(UserSelectionState.Loading)
    val state = _state.asStateFlow()

    private val _events = Channel<UserSelectionEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadUsers()
    }

    fun onAction(action: UserSelectionAction) {
        when (action) {
            is UserSelectionAction.OnUserClick -> {
                logger.d { "User selected: ${action.user.nickname} (${action.user.id})" }
                if (pairingToken != null) {
                    confirmPairing(action.user.id)
                } else {
                    // If no pairing token, we probably just navigate to PIN entry for this user
                    viewModelScope.launch {
                        _events.send(UserSelectionEvent.PairingConfirmed(action.user.id))
                    }
                }
            }
        }
    }

    private fun loadUsers() {
        logger.d { "Loading users for selection (token: ${pairingToken != null}, familyId: $familyId)" }
        viewModelScope.launch {
            _state.value = UserSelectionState.Loading
            val result = when {
                pairingToken != null -> authRepository.getPairingUsers(pairingToken)
                familyId != null -> authRepository.getFamilyUsers(familyId)
                else -> {
                    _state.value = UserSelectionState.Error("Missing identification parameters")
                    return@launch
                }
            }

            result
                .onSuccess { users ->
                    _state.value = UserSelectionState.Success(users = users)
                }
                .onFailure { error ->
                    _state.value = UserSelectionState.Error(error.toString())
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
                    _events.send(UserSelectionEvent.PairingConfirmed(user.id))
                }
                .onFailure { error ->
                    _state.value = currentSuccess.copy(isConfirming = false, error = error.toString())
                }
        }
    }
}
