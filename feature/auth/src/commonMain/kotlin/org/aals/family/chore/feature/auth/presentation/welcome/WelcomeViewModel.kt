package org.aals.family.chore.feature.auth.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.discovery.ServerDiscovery
import org.aals.family.chore.core.domain.repository.TokenStorage

class WelcomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(WelcomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<WelcomeEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: WelcomeAction) {
        when (action) {
            WelcomeAction.OnSetupNewFamilyClick -> {
                viewModelScope.launch {
                    _events.send(WelcomeEvent.NavigateToSetupFamily)
                }
            }
            WelcomeAction.OnJoinFamilyClick -> {
                viewModelScope.launch {
                    _events.send(WelcomeEvent.NavigateToJoinFamily)
                }
            }
        }
    }
}
