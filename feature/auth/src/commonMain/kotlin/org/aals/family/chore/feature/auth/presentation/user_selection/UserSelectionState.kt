package org.aals.family.chore.feature.auth.presentation.user_selection

import org.aals.family.chore.core.domain.model.User

data class UserSelectionState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
