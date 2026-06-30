package org.aals.family.chore.feature.auth.presentation.pin_entry

sealed interface PinEntryState {
    val pin: String

    data class Entering(
        override val pin: String = "",
        val error: String? = null
    ) : PinEntryState

    data class Verifying(
        override val pin: String
    ) : PinEntryState
}
