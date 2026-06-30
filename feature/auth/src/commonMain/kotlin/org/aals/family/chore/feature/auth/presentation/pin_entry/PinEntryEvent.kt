package org.aals.family.chore.feature.auth.presentation.pin_entry

sealed interface PinEntryEvent {
    data object PinVerified : PinEntryEvent
}
