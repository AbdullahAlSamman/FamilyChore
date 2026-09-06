package org.aals.family.chore.feature.auth.presentation.welcome

import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.presentation.UiText

data class WelcomeState(
    val serverName: String? = null,
    val families: List<Family> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isOfflineMode: Boolean = false,
    val isServerReachable: Boolean = true
)
