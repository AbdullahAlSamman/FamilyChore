package org.aals.family.chore.core.data.local.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(primaryKeys = ["id", "familyId"])
data class ChoreEntity(
    val id: String,
    val familyId: String,
    val title: String,
    val description: String,
    val points: Int,
    val isCompleted: Boolean,
    val verificationPhotoUrl: String?,
    val assigneeId: String,
    val version: Long
)
