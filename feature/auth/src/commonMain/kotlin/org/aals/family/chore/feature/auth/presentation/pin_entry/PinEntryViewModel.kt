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

    private val _state = MutableStateFlow<PinEntryState>(PinEntryState.Entering())
    val state = _state.asStateFlow()

    private val _events = Channel<PinEntryEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: PinEntryAction) {
        val currentState = _state.value
        if (currentState !is PinEntryState.Entering) return

        when (action) {
            is PinEntryAction.OnPinChange -> {
                if (action.pin.length <= 4) {
                    _state.value = currentState.copy(pin = action.pin, error = null)
                }
            }
            PinEntryAction.OnSubmit -> {
                if (currentState.pin.length == 4) {
                    submitPin()
                } else {
                    _state.value = currentState.copy(error = "PIN must be 4 digits")
                }
            }
        }
    }

    private fun submitPin() {
        val currentState = _state.value as? PinEntryState.Entering ?: return
        val pin = currentState.pin

        viewModelScope.launch {
            _state.value = PinEntryState.Verifying(pin)

            val result = if (isSetupMode) {
                authRepository.setupPin(userId, pin)
            } else {
                authRepository.verifyPin(userId, pin)
            }

            result
                .onSuccess {
                    _events.send(PinEntryEvent.PinVerified)
                }
                .onFailure { error ->
                    _state.value = PinEntryState.Entering(pin = pin, error = error.toString())
                }
        }
    }
}
