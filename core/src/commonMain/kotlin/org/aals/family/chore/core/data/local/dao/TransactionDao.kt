package org.aals.family.chore.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.aals.family.chore.core.data.local.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE familyId = :familyId ORDER BY timestamp DESC")
    fun getTransactionsForFamily(familyId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE familyId = :familyId")
    suspend fun clearTransactions(familyId: String)
}
