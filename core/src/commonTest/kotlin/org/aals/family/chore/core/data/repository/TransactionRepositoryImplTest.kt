package org.aals.family.chore.core.data.repository

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.aals.family.chore.core.data.local.dao.TransactionDao
import org.aals.family.chore.core.data.local.entity.TransactionEntity
import org.aals.family.chore.core.data.remote.TransactionDataSource
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.TransactionType
import org.aals.family.chore.core.domain.util.Result
import kotlin.test.BeforeTest
import kotlin.test.Test

class TransactionRepositoryImplTest {

    private lateinit var repository: TransactionRepositoryImpl
    private lateinit var dao: FakeTransactionDao
    private lateinit var dataSource: FakeTransactionDataSource
    private lateinit var tokenStorage: FakeTokenStorage
    private val logger = Logger.withTag("Test")

    @BeforeTest
    fun setup() {
        dao = FakeTransactionDao()
        dataSource = FakeTransactionDataSource()
        tokenStorage = FakeTokenStorage()
        repository = TransactionRepositoryImpl(dao, dataSource, tokenStorage, logger)
    }

    @Test
    fun `getTransactionsForUser maps entities to domain models`() = runTest {
        val userId = "user1"
        val familyId = "family1"
        val entity = TransactionEntity(
            id = "t1",
            familyId = familyId,
            userId = userId,
            adminId = null,
            amount = 10,
            type = TransactionType.CHORE,
            timestamp = 123456L,
            note = "Well done"
        )
        dao.transactionsFlow = flowOf(listOf(entity))

        repository.getTransactionsForUser(userId).test {
            val transactions = awaitItem()
            assertThat(transactions.size).isEqualTo(1)
            assertThat(transactions[0].id).isEqualTo("t1")
            assertThat(transactions[0].amount).isEqualTo(10)
            // Flow from Room doesn't complete, but Turbine 1.x might wait for more.
            // In Room KMP, it's a hot flow.
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTransaction inserts into local dao and pushes to data source`() = runTest {
        val transaction = Transaction(
            id = "t1",
            familyId = "f1",
            userId = "u1",
            adminId = null,
            amount = 10,
            type = TransactionType.CHORE,
            timestamp = 123L
        )

        val result = repository.addTransaction(transaction)

        assertThat(result is Result.Success).isEqualTo(true)
        assertThat(dao.insertedTransactions.size).isEqualTo(1)
        assertThat(dao.insertedTransactions[0].id).isEqualTo("t1")
        assertThat(dataSource.pushedTransactions.size).isEqualTo(1)
        assertThat(dataSource.pushedTransactions[0].id).isEqualTo("t1")
    }
}

private class FakeTransactionDao : TransactionDao {
    var transactionsFlow = flowOf(emptyList<TransactionEntity>())
    val insertedTransactions = mutableListOf<TransactionEntity>()

    override fun getTransactionsForUser(userId: String) = transactionsFlow
    override fun getTransactionsForFamily(familyId: String) = transactionsFlow
    override suspend fun insertTransaction(transaction: TransactionEntity) {
        insertedTransactions.add(transaction)
    }
    override suspend fun insertTransactions(transactions: List<TransactionEntity>) {
        insertedTransactions.addAll(transactions)
    }
    override suspend fun clearTransactions(familyId: String) {}
}

private class FakeTransactionDataSource : TransactionDataSource(
    HttpClient(MockEngine { respondOk() })
) {
    val pushedTransactions = mutableListOf<Transaction>()
    override suspend fun getTransactions(familyId: String, userId: String?) = Result.Success(emptyList<Transaction>())
    override suspend fun addTransaction(transaction: Transaction) = Result.Success(Unit).also {
        pushedTransactions.add(transaction)
    }
}
