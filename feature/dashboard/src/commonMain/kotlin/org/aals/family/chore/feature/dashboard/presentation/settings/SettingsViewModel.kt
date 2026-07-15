package org.aals.family.chore.feature.dashboard.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.repository.TokenStorage

class SettingsViewModel(
    private val tokenStorage: TokenStorage
) : ViewModel() {

    val state: StateFlow<SettingsState> = tokenStorage.language
        .map { languageCode ->
            val language = AppLanguage.entries.find { it.isoCode == languageCode } ?: AppLanguage.ENGLISH
            SettingsState(language = language)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsState()
        )

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.ChangeLanguage -> {
                viewModelScope.launch {
                    tokenStorage.saveLanguage(action.language.isoCode)
                }
            }
        }
    }
}

data class SettingsState(
    val language: AppLanguage = AppLanguage.ENGLISH
)

sealed interface SettingsAction {
    data class ChangeLanguage(val language: AppLanguage) : SettingsAction
}
