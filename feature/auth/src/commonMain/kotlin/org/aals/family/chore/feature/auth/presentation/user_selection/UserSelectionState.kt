package org.aals.family.chore.feature.auth.presentation.user_selection

import org.aals.family.chore.core.domain.model.User

sealed interface UserSelectionState {
    data object Loading : UserSelectionState
    data class Success(
        val users: List<User>,
        val isConfirming: Boolean = false,
        val error: String? = null
    ) : UserSelectionState
    data class Error(val message: String) : UserSelectionState
}
