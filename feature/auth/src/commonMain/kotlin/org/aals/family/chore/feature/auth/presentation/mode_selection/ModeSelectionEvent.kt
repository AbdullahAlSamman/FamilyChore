package org.aals.family.chore.feature.auth.presentation.mode_selection

sealed interface ModeSelectionEvent {
    data object NavigateToWelcome : ModeSelectionEvent
    data object NavigateToDiscovery : ModeSelectionEvent
}
