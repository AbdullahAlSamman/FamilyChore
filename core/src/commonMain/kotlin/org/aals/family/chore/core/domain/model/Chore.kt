package org.aals.family.chore.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Recurrence {
    NONE,
    CUSTOM,
    DAILY,
    WEEKLY,
    MONTHLY
}

@Serializable
data class Chore(
    val id: String,
    val familyId: String,
    val name: String,
    val description: String?,
    val points: Int,
    val status: ChoreStatus,
    val assignedTo: String,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long,
    val recurrence: Recurrence = Recurrence.NONE,
    val activeDays: List<Int> = emptyList(), // 1=Monday .. 7=Sunday (ISO-8601)
    val activeWindowStart: Int? = null, // minutes from midnight
    val activeWindowEnd: Int? = null,
    val penalty: Int = 0,
    val requiresPhotoProof: Boolean = false,
    val iconName: String? = null,
    val parentChoreId: String? = null, // null if this is a template
    val dueDate: Long? = null, // timestamp for the specific instance
    val proofFileId: String? = null
)

@Serializable
enum class ChoreStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}

fun Chore.toEntity(): org.aals.family.chore.core.data.local.entity.ChoreEntity = org.aals.family.chore.core.data.local.entity.ChoreEntity(
    id = id,
    familyId = familyId,
    name = name,
    description = description,
    points = points,
    status = status.name,
    assignedTo = assignedTo,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt,
    recurrence = recurrence.name,
    activeDays = activeDays.joinToString(","),
    activeWindowStart = activeWindowStart,
    activeWindowEnd = activeWindowEnd,
    penalty = penalty,
    requiresPhotoProof = requiresPhotoProof,
    iconName = iconName,
    parentChoreId = parentChoreId,
    dueDate = dueDate
)
