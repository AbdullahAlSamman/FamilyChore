package org.aals.family.chore.feature.auth.presentation.welcome

sealed interface WelcomeAction {
    data object OnSetupNewFamilyClick : WelcomeAction
    data object OnJoinFamilyClick : WelcomeAction
}
