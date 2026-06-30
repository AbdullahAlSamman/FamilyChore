package org.aals.family.chore.feature.auth.presentation.pin_entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess

class PinEntryViewModel(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])
    private val isSetupMode: Boolean = savedStateHandle["isSetupMode"] ?: false

    private val _state = MutableStateFlow(PinEntryState())
    val state = _state.asStateFlow()

    private val _events = Channel<PinEntryEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: PinEntryAction) {
        when (action) {
            is PinEntryAction.OnPinChange -> {
                if (action.pin.length <= 4) {
                    _state.update { it.copy(pin = action.pin, error = null) }
                }
            }
            PinEntryAction.OnSubmit -> {
                if (_state.value.pin.length == 4) {
                    submitPin()
                } else {
                    _state.update { it.copy(error = "PIN must be 4 digits") }
                }
            }
        }
    }

    private fun submitPin() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val pin = _state.value.pin
            val result = if (isSetupMode) {
                authRepository.setupPin(userId, pin)
            } else {
                authRepository.verifyPin(userId, pin)
            }

            result
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(PinEntryEvent.PinVerified)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toString()) }
                }
        }
    }
}
