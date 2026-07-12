package org.aals.family.chore.core.data.remote.dto

import kotlinx.serialization.Serializable
import org.aals.family.chore.core.domain.model.ChoreStatus

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
    val updatedAt: Long
)

@Serializable
data class CreateChoreRequest(
    val familyId: String,
    val name: String,
    val description: String?,
    val points: Int,
    val assignedTo: String,
    val createdBy: String
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
