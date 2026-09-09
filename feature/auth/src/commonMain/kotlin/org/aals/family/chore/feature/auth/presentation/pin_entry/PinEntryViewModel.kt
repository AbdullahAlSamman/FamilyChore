package org.aals.family.chore.feature.auth.presentation.pin_entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.auth_pin_invalid_length_error
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess
import org.aals.family.chore.core.presentation.UiText
import org.aals.family.chore.core.presentation.toUiText

class PinEntryViewModel(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
    private val logger: Logger
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])
    private val isSetupMode: Boolean = savedStateHandle["isSetupMode"] ?: false

    private val _state = MutableStateFlow<PinEntryState>(
        if (isSetupMode) PinEntryState.Entering(isSetupMode = true)
        else PinEntryState.Checking()
    )
    val state = _state.asStateFlow()

    private val _events = Channel<PinEntryEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadUserAndCheckRequirement()
    }

    private fun loadUserAndCheckRequirement() {
        if (isSetupMode) return

        viewModelScope.launch {
            authRepository.getUser(userId)
                .onSuccess { user ->
                    val isParent = user.role == UserRole.PARENT
                    if (!isParent && !user.requiresPin) {
                        logger.d { "PIN not required for child, bypassing" }
                        _events.send(PinEntryEvent.PinVerified)
                    } else {
                        _state.value = PinEntryState.Entering(isSetupMode = false)
                    }
                }
                .onFailure { e ->
                    logger.e { "Failed to fetch user profile: $e" }
                    _state.value = PinEntryState.Entering(error = e.toUiText(), isSetupMode = false)
                }
        }
    }

    fun onAction(action: PinEntryAction) {
        val currentState = _state.value
        
        if (action == PinEntryAction.OnBackClick) {
            viewModelScope.launch {
                _events.send(PinEntryEvent.NavigateBack)
            }
            return
        }

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
                    _state.value = currentState.copy(error = UiText.StringResource(Res.string.auth_pin_invalid_length_error))
                }
            }
            PinEntryAction.OnBackClick -> Unit
        }
    }

    private fun submitPin() {
        val currentState = _state.value as? PinEntryState.Entering ?: return
        val pin = currentState.pin

        viewModelScope.launch {
            logger.d { "Submitting PIN (isSetupMode=$isSetupMode)" }
            _state.value = PinEntryState.Verifying(pin, isSetupMode = isSetupMode)

            val result = if (isSetupMode) {
                authRepository.setupPin(userId, pin)
            } else {
                authRepository.verifyPin(userId, pin)
            }

            result
                .onSuccess {
                    logger.d { "PIN operation successful" }
                    _events.send(PinEntryEvent.PinVerified)
                }
                .onFailure { error ->
                    logger.e { "PIN operation failed for user $userId: $error" }
                    _state.value = PinEntryState.Entering(pin = pin, error = error.toUiText(), isSetupMode = isSetupMode)
                }
        }
    }
}
