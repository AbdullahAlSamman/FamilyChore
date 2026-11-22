package org.aals.family.chore.core.data.local.entity

import androidx.room.Entity

@Entity(primaryKeys = ["id", "familyId"])
data class ChoreEntity(
    val id: String,
    val familyId: String,
    val name: String,
    val description: String?,
    val points: Int,
    val status: String, // Stored as name of ChoreStatus enum
    val assignedTo: String,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long,
    val recurrence: String = "NONE", // Recurrence enum name
    val activeDays: String = "", // Comma-separated list of integers
    val activeWindowStart: Int? = null,
    val activeWindowEnd: Int? = null,
    val penalty: Int = 0,
    val requiresPhotoProof: Boolean = false,
    val iconName: String? = null,
    val parentChoreId: String? = null,
    val dueDate: Long? = null,
    val proofFileId: String? = null
)
