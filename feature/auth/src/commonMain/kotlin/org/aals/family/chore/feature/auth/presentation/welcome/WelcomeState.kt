package org.aals.family.chore.feature.auth.presentation.welcome

import org.aals.family.chore.core.domain.model.Family

data class WelcomeState(
    val serverName: String? = null,
    val families: List<Family> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
