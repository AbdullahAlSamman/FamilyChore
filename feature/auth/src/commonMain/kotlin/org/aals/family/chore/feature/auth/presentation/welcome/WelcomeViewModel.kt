package org.aals.family.chore.feature.auth.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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
