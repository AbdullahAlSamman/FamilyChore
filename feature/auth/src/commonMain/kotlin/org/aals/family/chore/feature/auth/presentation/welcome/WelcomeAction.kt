package org.aals.family.chore.feature.auth.presentation.welcome

import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.model.Family

sealed interface WelcomeAction {
    data object OnSetupNewFamilyClick : WelcomeAction
    data object OnJoinFamilyClick : WelcomeAction
    data object OnBackClick : WelcomeAction
    data class OnFamilyClick(val family: Family) : WelcomeAction
    data class OnChangeLanguage(val language: AppLanguage) : WelcomeAction
}
