package org.aals.family.chore.feature.auth.presentation.welcome

sealed interface WelcomeEvent {
    data object NavigateToSetupFamily : WelcomeEvent
    data object NavigateToJoinFamily : WelcomeEvent
    data object NavigateToModeSelection : WelcomeEvent
    data class NavigateToUserSelection(val familyId: String) : WelcomeEvent
}
