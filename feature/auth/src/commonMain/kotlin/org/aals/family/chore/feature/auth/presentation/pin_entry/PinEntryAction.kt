package org.aals.family.chore.feature.auth.presentation.pin_entry

sealed interface PinEntryAction {
    data class OnPinChange(val pin: String) : PinEntryAction
    data object OnSubmit : PinEntryAction
    data object OnSkip : PinEntryAction
}
