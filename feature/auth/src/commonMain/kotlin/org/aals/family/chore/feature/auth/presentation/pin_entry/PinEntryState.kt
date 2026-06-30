package org.aals.family.chore.feature.auth.presentation.pin_entry

data class PinEntryState(
    val pin: String = "",
    val error: String? = null,
    val isLoading: Boolean = false
)
