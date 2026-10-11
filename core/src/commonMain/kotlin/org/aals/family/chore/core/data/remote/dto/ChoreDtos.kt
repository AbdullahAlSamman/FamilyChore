package org.aals.family.chore.core.data.remote.dto

import kotlinx.serialization.Serializable
import org.aals.family.chore.core.domain.model.ChoreStatus
import org.aals.family.chore.core.domain.model.Recurrence

@Serializable
data class ChoreDto(
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
    val activeDays: List<Int> = emptyList(),
    val activeWindowStart: Int? = null,
    val activeWindowEnd: Int? = null,
    val penalty: Int = 0,
    val requiresPhotoProof: Boolean = false,
    val iconName: String? = null,
    val parentChoreId: String? = null,
    val dueDate: Long? = null
)

@Serializable
data class CreateChoreRequest(
    val familyId: String,
    val name: String,
    val description: String?,
    val points: Int,
    val assignedTo: String,
    val createdBy: String,
    val recurrence: Recurrence = Recurrence.NONE,
    val activeDays: List<Int> = emptyList(),
    val activeWindowStart: Int? = null,
    val activeWindowEnd: Int? = null,
    val penalty: Int = 0,
    val requiresPhotoProof: Boolean = false,
    val iconName: String? = null,
    val parentChoreId: String? = null,
    val dueDate: Long? = null
)

@Serializable
data class UpdateChoreStatusRequest(
    val choreId: String,
    val familyId: String,
    val newStatus: ChoreStatus,
    val adminId: String? = null
)

@Serializable
data class ChoresResponse(
    val chores: List<ChoreDto>
)
