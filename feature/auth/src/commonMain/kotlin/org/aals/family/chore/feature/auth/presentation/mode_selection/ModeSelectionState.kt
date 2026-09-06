package org.aals.family.chore.feature.auth.presentation.mode_selection

import org.aals.family.chore.core.domain.model.AppLanguage

sealed interface ModeSelectionState {
    val currentLanguage: AppLanguage

    data class Loading(
        override val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    ) : ModeSelectionState

    data class Content(
        override val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    ) : ModeSelectionState
}
