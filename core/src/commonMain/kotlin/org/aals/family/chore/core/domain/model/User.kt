package org.aals.family.chore.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val familyId: String,
    val nickname: String,
    val role: UserRole,
    val points: Int = 0
)

@Serializable
enum class UserRole { PARENT, CHILD }
