package org.aals.family.chore.feature.auth.presentation.user_selection

import org.aals.family.chore.core.domain.model.User

/**
 * Represents the state of the user profile selection screen.
 * Handles the loading of family members and the confirmation of the selection.
 */
sealed interface UserSelectionState {
    /** Fetching the list of family members from the server. */
    data object Loading : UserSelectionState
    
    /** Successfully fetched the list of users. */
    data class Success(
        val users: List<User>,
        val isConfirming: Boolean = false,
        val error: String? = null
    ) : UserSelectionState
    
    /** Failed to fetch or select a user. */
    data class Error(val message: String) : UserSelectionState
}
