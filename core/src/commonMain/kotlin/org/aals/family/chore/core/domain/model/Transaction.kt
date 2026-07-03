package org.aals.family.chore.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val familyId: String,
    val userId: String,
    val adminId: String?,
    val amount: Int,
    val type: TransactionType,
    val timestamp: Long,
    val note: String? = null
)

@Serializable
enum class TransactionType {
    CHORE,
    BONUS,
    PENALTY
}
