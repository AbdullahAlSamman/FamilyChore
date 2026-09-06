package org.aals.family.chore.core.data.local.entity

import androidx.room.Entity

@Entity(primaryKeys = ["id", "familyId"])
data class UserEntity(
    val id: String,
    val familyId: String,
    val name: String,
    val role: String,
    val pin: String?,
    val requiresPin: Boolean
)
