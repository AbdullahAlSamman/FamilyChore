package org.aals.family.chore.feature.auth.presentation.create_family

data class CreateFamilyState(
    val familyName: String = "",
    val parentNickname: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
