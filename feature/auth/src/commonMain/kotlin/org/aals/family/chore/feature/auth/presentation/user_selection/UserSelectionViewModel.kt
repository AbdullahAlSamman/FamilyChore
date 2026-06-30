package org.aals.family.chore.feature.auth.presentation.user_selection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess

class UserSelectionViewModel(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pairingToken: String = checkNotNull(savedStateHandle["pairingToken"])

    private val _state = MutableStateFlow<UserSelectionState>(UserSelectionState.Loading)
    val state = _state.asStateFlow()

    private val _events = Channel<UserSelectionEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadUsers()
    }

    fun onAction(action: UserSelectionAction) {
        when (action) {
            is UserSelectionAction.OnUserClick -> confirmPairing(action.user.id)
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _state.value = UserSelectionState.Loading
            authRepository.getPairingUsers(pairingToken)
                .onSuccess { users ->
                    _state.value = UserSelectionState.Success(users = users)
                }
                .onFailure { error ->
                    _state.value = UserSelectionState.Error(error.toString())
                }
        }
    }

    private fun confirmPairing(userId: String) {
        val currentSuccess = _state.value as? UserSelectionState.Success ?: return

        viewModelScope.launch {
            _state.value = currentSuccess.copy(isConfirming = true, error = null)
            authRepository.confirmPairing(pairingToken, userId)
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
