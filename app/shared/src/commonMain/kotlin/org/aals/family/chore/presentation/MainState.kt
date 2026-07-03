package org.aals.family.chore.presentation

sealed interface MainState {
    data object Loading : MainState
    data class Success(val startDestination: Any) : MainState
}
