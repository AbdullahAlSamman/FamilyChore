package org.aals.family.chore.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.aals.family.chore.core.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val userId: String,
    val adminId: String?,
    val amount: Int,
    val type: TransactionType,
    val timestamp: Long,
    val note: String?
)
