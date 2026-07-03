package org.aals.family.chore.domain.repository

import org.aals.family.chore.core.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactionsForUser(userId: String): List<Transaction>
    suspend fun getTransactionsForFamily(familyId: String): List<Transaction>
    suspend fun addTransaction(transaction: Transaction)
}
