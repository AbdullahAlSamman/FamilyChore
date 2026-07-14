package org.aals.family.chore.presentation

import org.aals.family.chore.core.domain.model.AppLanguage

sealed interface MainState {
    data object Loading : MainState
    data class Success(
        val startDestination: Any,
        val language: AppLanguage = AppLanguage.ENGLISH
    ) : MainState
}
