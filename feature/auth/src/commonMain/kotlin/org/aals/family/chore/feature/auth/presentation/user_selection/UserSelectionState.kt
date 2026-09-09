package org.aals.family.chore.feature.auth.presentation.user_selection

import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.presentation.UiText

/**
 * Represents the state of the user profile selection screen.
 * Handles the loading of family members and the confirmation of the selection.
 */
sealed interface UserSelectionState {
    val isOfflineMode: Boolean
    val isServerReachable: Boolean

    /** Fetching the list of family members from the server. */
    data class Loading(
        override val isOfflineMode: Boolean = false,
        override val isServerReachable: Boolean = true
    ) : UserSelectionState
    
    /** Successfully fetched the list of users. */
    data class Success(
        val users: List<User>,
        val isConfirming: Boolean = false,
        val isFromDiscovery: Boolean = false,
        override val isOfflineMode: Boolean = false,
        override val isServerReachable: Boolean = true,
        val error: UiText? = null
    ) : UserSelectionState
    
    /** Failed to fetch or select a user. */
    data class Error(
        val message: UiText,
        override val isOfflineMode: Boolean = false,
        override val isServerReachable: Boolean = true
    ) : UserSelectionState
}
