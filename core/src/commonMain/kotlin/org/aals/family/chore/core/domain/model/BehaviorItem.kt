package org.aals.family.chore.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BehaviorItem(
    val id: String,
    val familyId: String,
    val name: String,
    val points: Int,
    val isPositive: Boolean = points >= 0
)
