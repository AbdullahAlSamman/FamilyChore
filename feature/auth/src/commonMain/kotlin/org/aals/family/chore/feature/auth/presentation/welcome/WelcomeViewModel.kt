package org.aals.family.chore.feature.auth.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.repository.TokenStorage
import co.touchlab.kermit.Logger

class WelcomeViewModel(
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _state = MutableStateFlow(WelcomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<WelcomeEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val name = tokenStorage.getServerName()
            _state.update { it.copy(serverName = name) }
        }
    }

    fun onAction(action: WelcomeAction) {
        when (action) {
            WelcomeAction.OnSetupNewFamilyClick -> {
                Logger.d { "User chose: Setup New Family" }
                viewModelScope.launch {
                    _events.send(WelcomeEvent.NavigateToSetupFamily)
                }
            }
            WelcomeAction.OnJoinFamilyClick -> {
                Logger.d { "User chose: Join Existing Family" }
                viewModelScope.launch {
                    _events.send(WelcomeEvent.NavigateToJoinFamily)
                }
            }
        }
    }
}
