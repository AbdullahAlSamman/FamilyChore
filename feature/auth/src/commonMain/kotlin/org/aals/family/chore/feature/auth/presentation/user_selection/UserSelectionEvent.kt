package org.aals.family.chore.feature.auth.presentation.user_selection

sealed interface UserSelectionEvent {
    data class PairingConfirmed(val userId: String) : UserSelectionEvent
}
