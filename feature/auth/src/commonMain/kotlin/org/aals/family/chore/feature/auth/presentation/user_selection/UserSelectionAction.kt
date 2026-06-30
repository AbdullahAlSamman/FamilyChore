package org.aals.family.chore.feature.auth.presentation.user_selection

import org.aals.family.chore.core.domain.model.User

sealed interface UserSelectionAction {
    data class OnUserClick(val user: User) : UserSelectionAction
}
