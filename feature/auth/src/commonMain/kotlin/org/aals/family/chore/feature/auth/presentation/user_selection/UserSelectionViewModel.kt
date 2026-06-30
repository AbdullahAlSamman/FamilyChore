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

    private val _state = MutableStateFlow(UserSelectionState())
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
            _state.update { it.copy(isLoading = true) }
            authRepository.getPairingUsers(pairingToken)
                .onSuccess { users ->
                    _state.update { it.copy(users = users, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toString()) }
                }
        }
    }

    private fun confirmPairing(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.confirmPairing(pairingToken, userId)
                .onSuccess { user ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(UserSelectionEvent.PairingConfirmed(user.id))
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toString()) }
                }
        }
    }
}
