package org.aals.family.chore.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PairingToken(
    val token: String,
    val serverIp: String,
    val familyId: String? = null,
    val userId: String? = null
)
