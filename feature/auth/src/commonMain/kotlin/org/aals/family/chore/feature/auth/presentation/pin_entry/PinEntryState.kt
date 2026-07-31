package org.aals.family.chore.feature.auth.presentation.pin_entry

import org.aals.family.chore.core.presentation.UiText

/**
 * Represents the state of the PIN entry flow.
 * Supports both entering the PIN and the verification phase.
 */
sealed interface PinEntryState {
    /** The current value of the PIN digits. */
    val pin: String
    
    /** Whether the screen is in setup mode (first time setting PIN). */
    val isSetupMode: Boolean

    /** The app is checking if a PIN is required for this user. */
    data class Checking(
        override val pin: String = "",
        override val isSetupMode: Boolean = false
    ) : PinEntryState

    /** The user is actively typing the PIN. */
    data class Entering(
        override val pin: String = "",
        override val isSetupMode: Boolean = false,
        val error: UiText? = null
    ) : PinEntryState

    /** The PIN has been submitted and is being verified by the server. */
    data class Verifying(
        override val pin: String,
        override val isSetupMode: Boolean = false
    ) : PinEntryState
}
