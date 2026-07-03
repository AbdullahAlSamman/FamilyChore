package org.aals.family.chore.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

interface TransactionRepository {
    fun getTransactionsForUser(userId: String): Flow<List<Transaction>>
    fun getTransactionsForFamily(familyId: String): Flow<List<Transaction>>
    suspend fun syncTransactions(familyId: String): Result<Unit, DataError>
    suspend fun addTransaction(transaction: Transaction): Result<Unit, DataError>
}
