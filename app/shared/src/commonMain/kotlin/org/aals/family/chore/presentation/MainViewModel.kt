package org.aals.family.chore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.feature.auth.presentation.navigation.ModeSelectionRoute
import org.aals.family.chore.feature.auth.presentation.navigation.ServerDiscoveryRoute
import org.aals.family.chore.feature.auth.presentation.navigation.UserSelectionRoute
import org.aals.family.chore.feature.auth.presentation.navigation.WelcomeRoute

class MainViewModel(
    private val tokenStorage: TokenStorage,
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MainState>(MainState.Loading)
    val state = _state.asStateFlow()

    init {
        checkInitialState()
        observeLanguage()
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            tokenStorage.saveLanguage(language.isoCode)
        }
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            val isOffline = tokenStorage.getOfflineMode()
            val serverUrl = tokenStorage.getServerUrl()
            val familyId = tokenStorage.getFamilyId()
            val token = tokenStorage.getToken()

            val destination = if (token != null || familyId != null) {
                UserSelectionRoute(familyId = familyId)
            } else if (isOffline == null) {
                ModeSelectionRoute
            } else if (isOffline) {
                WelcomeRoute
            } else if (serverUrl != null) {
                val result = connectivityRepository.checkHealth()
                if (result is Result.Success) {
                    WelcomeRoute
                } else {
                    ServerDiscoveryRoute(isErrorMode = true)
                }
            } else {
                ServerDiscoveryRoute()
            }

            _state.value = MainState.Success(
                startDestination = destination,
                language = AppLanguage.entries.find { it.isoCode == tokenStorage.getLanguage() } ?: AppLanguage.ENGLISH
            )
        }
    }

    private fun observeLanguage() {
        tokenStorage.language
            .onEach { languageCode ->
                val newLang = AppLanguage.entries.find { it.isoCode == languageCode } ?: AppLanguage.ENGLISH
                _state.update { currentState ->
                    if (currentState is MainState.Success) {
                        currentState.copy(language = newLang)
                    } else {
                        currentState
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}
