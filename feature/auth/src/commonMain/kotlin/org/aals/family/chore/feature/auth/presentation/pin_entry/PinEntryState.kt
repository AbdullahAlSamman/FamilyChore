package org.aals.family.chore.feature.auth.presentation.pin_entry

/**
 * Represents the state of the PIN entry flow.
 * Supports both entering the PIN and the verification phase.
 */
sealed interface PinEntryState {
    /** The current value of the PIN digits. */
    val pin: String

    /** The user is actively typing the PIN. */
    data class Entering(
        override val pin: String = "",
        val error: String? = null
    ) : PinEntryState

    /** The PIN has been submitted and is being verified by the server. */
    data class Verifying(
        override val pin: String
    ) : PinEntryState
}
