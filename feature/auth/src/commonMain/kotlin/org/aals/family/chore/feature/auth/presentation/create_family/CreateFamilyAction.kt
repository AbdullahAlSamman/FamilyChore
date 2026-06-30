package org.aals.family.chore.feature.auth.presentation.create_family

sealed interface CreateFamilyAction {
    data class OnFamilyNameChange(val name: String) : CreateFamilyAction
    data class OnParentNicknameChange(val nickname: String) : CreateFamilyAction
    data object OnCreateClick : CreateFamilyAction
    data object OnBackClick : CreateFamilyAction
}
