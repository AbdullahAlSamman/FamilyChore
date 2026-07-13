package org.aals.family.chore.feature.auth.presentation.create_family

import org.aals.family.chore.core.presentation.UiText

data class CreateFamilyState(
    val familyName: String = "",
    val parentNickname: String = "",
    val isLoading: Boolean = false,
    val familyNameError: UiText? = null,
    val nicknameError: UiText? = null,
    val error: String? = null
)
