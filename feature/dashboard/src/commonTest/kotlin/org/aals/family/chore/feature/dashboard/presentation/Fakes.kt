package org.aals.family.chore.feature.dashboard.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TransactionRepository
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

class FakeAuthRepository : AuthRepository {
    var currentUser: User? = null
    override suspend fun createFamily(familyName: String, parentNickname: String): Result<User, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun getFamilies(): Result<List<Family>, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun generatePairingToken(familyId: String): Result<String, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun getPairingUsers(pairingToken: String): Result<List<User>, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun getFamilyUsers(familyId: String): Result<List<User>, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun confirmPairing(pairingToken: String, userId: String): Result<User, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun setupPin(userId: String, pin: String): Result<Unit, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun verifyPin(userId: String, pin: String): Result<Unit, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun getCurrentUser(): Result<User, DataError.Network> {
        return currentUser?.let { Result.Success(it) } ?: Result.Error(DataError.Network.UNKNOWN)
    }
}

class FakeTransactionRepository : TransactionRepository {
    var transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val addedTransactions = mutableListOf<Transaction>()

    override fun getTransactionsForUser(userId: String): Flow<List<Transaction>> = transactions
    override fun getTransactionsForFamily(familyId: String): Flow<List<Transaction>> = transactions
    override suspend fun syncTransactions(familyId: String): Result<Unit, DataError> = Result.Success(Unit)
    override suspend fun addTransaction(transaction: Transaction): Result<Unit, DataError> {
        addedTransactions.add(transaction)
        return Result.Success(Unit)
    }
}

class FakeConnectivityRepository : ConnectivityRepository {
    override val isServerReachable = MutableStateFlow(true)
    override suspend fun checkHealth(): Result<Unit, DataError.Network> = Result.Success(Unit)
}
