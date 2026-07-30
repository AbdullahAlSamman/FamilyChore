package org.aals.family.chore.feature.auth.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess

class WelcomeViewModel(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage,
    private val logger: Logger
) : ViewModel() {

    private val _state = MutableStateFlow(WelcomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<WelcomeEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val name = tokenStorage.getServerName()
            val isOffline = tokenStorage.getOfflineMode() ?: false
            _state.update { it.copy(serverName = name, isOfflineMode = isOffline) }
        }
        observeLanguage()
        loadFamilies()
    }

    fun onAction(action: WelcomeAction) {
        when (action) {
            WelcomeAction.OnSetupNewFamilyClick -> {
                logger.d { "User chose: Setup New Family" }
                viewModelScope.launch {
                    _events.send(WelcomeEvent.NavigateToSetupFamily)
                }
            }
            WelcomeAction.OnJoinFamilyClick -> {
                logger.d { "User chose: Join Existing Family" }
                viewModelScope.launch {
                    _events.send(WelcomeEvent.NavigateToJoinFamily)
                }
            }
            WelcomeAction.OnBackClick -> {
                viewModelScope.launch {
                    tokenStorage.setOfflineMode(null)
                    _events.send(WelcomeEvent.NavigateToModeSelection)
                }
            }
            is WelcomeAction.OnFamilyClick -> {
                logger.d { "User clicked family: ${action.family.name}" }
                viewModelScope.launch {
                    tokenStorage.saveFamilyId(action.family.id)
                    _events.send(WelcomeEvent.NavigateToUserSelection(action.family.id))
                }
            }
            is WelcomeAction.OnChangeLanguage -> {
                viewModelScope.launch {
                    tokenStorage.saveLanguage(action.language.isoCode)
                }
            }
        }
    }

    private fun observeLanguage() {
        tokenStorage.language
            .onEach { langCode ->
                val currentLang = AppLanguage.entries.find { it.isoCode == langCode } ?: AppLanguage.ENGLISH
                _state.update { it.copy(currentLanguage = currentLang) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadFamilies() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.getFamilies()
                .onSuccess { families ->
                    _state.update { it.copy(families = families, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toString()) }
                }
        }
    }
}
