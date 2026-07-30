package org.aals.family.chore.core.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import org.aals.family.chore.core.data.local.dao.TransactionDao
import org.aals.family.chore.core.data.local.entity.TransactionEntity
import org.aals.family.chore.core.data.remote.TransactionDataSource
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.repository.TransactionRepository
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.map
import org.aals.family.chore.core.domain.util.onSuccess
import kotlinx.coroutines.flow.map as flowMap

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val transactionDataSource: TransactionDataSource,
    private val tokenStorage: TokenStorage,
    private val logger: Logger
) : TransactionRepository {

    override fun getTransactionsForUser(userId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForUser(userId).flowMap { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsForFamily(familyId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForFamily(familyId).flowMap { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncTransactions(familyId: String): Result<Unit, DataError> {
        logger.d { "Syncing transactions for family: ${familyId}" }
        if (tokenStorage.getOfflineMode() == true) {
            return Result.Success(Unit)
        }
        return transactionDataSource.getTransactions(familyId)
            .onSuccess { transactions ->
                transactionDao.insertTransactions(transactions.map { it.toEntity() })
            }
            .map { Unit }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit, DataError> {
        logger.d { "Adding transaction: ${transaction.id}" }
        transactionDao.insertTransaction(transaction.toEntity())
        if (tokenStorage.getOfflineMode() == true) {
            return Result.Success(Unit)
        }
        return transactionDataSource.addTransaction(transaction)
    }
}

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        familyId = familyId,
        userId = userId,
        adminId = adminId,
        amount = amount,
        type = type,
        timestamp = timestamp,
        note = note
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        familyId = familyId,
        userId = userId,
        adminId = adminId,
        amount = amount,
        type = type,
        timestamp = timestamp,
        note = note
    )
}
