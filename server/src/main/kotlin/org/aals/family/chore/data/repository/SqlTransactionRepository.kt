package org.aals.family.chore.data.repository

import co.touchlab.kermit.Logger
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.TransactionType
import org.aals.family.chore.data.local.DatabaseFactory.dbQuery
import org.aals.family.chore.data.local.TransactionsTable
import org.aals.family.chore.data.local.UsersTable
import org.aals.family.chore.domain.repository.TransactionRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class SqlTransactionRepository : TransactionRepository {

    override suspend fun getTransactionsForUser(userId: String): List<Transaction> = dbQuery {
        TransactionsTable.selectAll().where { TransactionsTable.userId eq userId }
            .orderBy(TransactionsTable.timestamp to SortOrder.DESC)
            .map { it.toTransaction() }
    }

    override suspend fun getTransactionsForFamily(familyId: String): List<Transaction> = dbQuery {
        TransactionsTable.selectAll().where { TransactionsTable.familyId eq familyId }
            .orderBy(TransactionsTable.timestamp to SortOrder.DESC)
            .map { it.toTransaction() }
    }

    override suspend fun addTransaction(transaction: Transaction): Unit = dbQuery {
        Logger.d { "Recording transaction in DB: ${transaction.id}" }
        TransactionsTable.insert {
            it[id] = transaction.id
            it[familyId] = transaction.familyId
            it[userId] = transaction.userId
            it[adminId] = transaction.adminId
            it[amount] = transaction.amount
            it[type] = transaction.type.name
            it[timestamp] = transaction.timestamp
            it[note] = transaction.note
        }
        
        // Update user's total points
        UsersTable.update({ UsersTable.id eq transaction.userId }) {
            with(SqlExpressionBuilder) {
                it[points] = points + transaction.amount
            }
        }
    }

    private fun ResultRow.toTransaction() = Transaction(
        id = this[TransactionsTable.id],
        familyId = this[TransactionsTable.familyId],
        userId = this[TransactionsTable.userId],
        adminId = this[TransactionsTable.adminId],
        amount = this[TransactionsTable.amount],
        type = TransactionType.valueOf(this[TransactionsTable.type]),
        timestamp = this[TransactionsTable.timestamp],
        note = this[TransactionsTable.note]
    )
}
