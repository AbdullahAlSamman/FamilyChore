package org.aals.family.chore.feature.auth.presentation.mode_selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.repository.TokenStorage

class ModeSelectionViewModel(
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _state = MutableStateFlow(ModeSelectionState())
    val state = _state.asStateFlow()

    private val _events = Channel<ModeSelectionEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeLanguage()
    }

    fun onAction(action: ModeSelectionAction) {
        when (action) {
            ModeSelectionAction.OnSingleDeviceClick -> {
                viewModelScope.launch {
                    tokenStorage.setOfflineMode(true)
                    _events.send(ModeSelectionEvent.NavigateToWelcome)
                }
            }
            ModeSelectionAction.OnMultipleDevicesClick -> {
                viewModelScope.launch {
                    tokenStorage.setOfflineMode(false)
                    _events.send(ModeSelectionEvent.NavigateToDiscovery)
                }
            }
            is ModeSelectionAction.OnChangeLanguage -> {
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
}
