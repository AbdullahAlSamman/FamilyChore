package org.aals.family.chore.feature.auth.presentation.create_family

data class CreateFamilyState(
    val serverIp: String = "http://192.168.1.100:8080",
    val familyName: String = "",
    val parentNickname: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
