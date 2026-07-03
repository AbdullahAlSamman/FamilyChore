package org.aals.family.chore.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Reward(
    val id: String,
    val familyId: String,
    val title: String,
    val description: String,
    val pointCost: Int
)
