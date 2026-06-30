package org.aals.family.chore.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Family(
    val id: String,
    val name: String
)
