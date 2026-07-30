package org.aals.family.chore.feature.auth.presentation.mode_selection

import org.aals.family.chore.core.domain.model.AppLanguage

data class ModeSelectionState(
    val isLoading: Boolean = false,
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH
)
