package org.aals.family.chore.feature.auth.presentation.mode_selection

import org.aals.family.chore.core.domain.model.AppLanguage

sealed interface ModeSelectionAction {
    data object OnSingleDeviceClick : ModeSelectionAction
    data object OnMultipleDevicesClick : ModeSelectionAction
    data class OnChangeLanguage(val language: AppLanguage) : ModeSelectionAction
}
