package org.aals.family.chore.core.domain.model

import kotlinx.serialization.Serializable

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
    val updatedAt: Long
)

enum class ChoreStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    APPROVED,
    REJECTED
}
