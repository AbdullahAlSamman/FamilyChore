package org.aals.family.chore.feature.dashboard.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.ChoreStatus
import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.ChoreRepository
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TransactionRepository
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.TimeProvider

class FakeTimeProvider(var staticTime: Long = 0L) : TimeProvider {
    override fun now(): Long = staticTime
}

class FakeAuthRepository : AuthRepository {
    var currentUser: User? = null
    var familyMembers = mutableListOf<User>()
    var nextError: DataError.Network? = null
    var nextPairingToken: String = "token123"
    
    override suspend fun createFamily(familyName: String, parentNickname: String): Result<User, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun getFamilies(): Result<List<Family>, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun generatePairingToken(familyId: String): Result<String, DataError.Network> {
        return nextError?.let { Result.Error(it) } ?: Result.Success(nextPairingToken)
    }
    override suspend fun getPairingUsers(pairingToken: String): Result<List<User>, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun getFamilyMembers(familyId: String): Result<List<User>, DataError.Network> {
        return Result.Success(familyMembers)
    }
    override suspend fun confirmPairing(pairingToken: String, userId: String): Result<User, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun setupPin(userId: String, pin: String): Result<Unit, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    override suspend fun verifyPin(userId: String, pin: String): Result<Unit, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)

    override suspend fun addFamilyMember(
        familyId: String,
        nickname: String,
        role: UserRole,
        pin: String?
    ): Result<User, DataError.Network> {
        return nextError?.let { Result.Error(it) } ?: run {
            val user = User("new", familyId, nickname, role, 0, pin != null)
            familyMembers.add(user)
            Result.Success(user)
        }
    }

    override suspend fun updateUserPinRequirement(
        userId: String,
        requiresPin: Boolean
    ): Result<Unit, DataError.Network> = Result.Success(Unit)

    override suspend fun getUser(userId: String): Result<User, DataError.Network> {
        val user = familyMembers.find { it.id == userId } ?: User(userId, "family1", "User", org.aals.family.chore.core.domain.model.UserRole.CHILD, 0)
        return Result.Success(user)
    }

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

class FakeChoreRepository : ChoreRepository {
    var chores = MutableStateFlow<List<Chore>>(emptyList())
    val createdChores = mutableListOf<Chore>()

    override fun getChoresForFamily(familyId: String): Flow<List<Chore>> = chores
    override fun getChoresForUser(userId: String): Flow<List<Chore>> = chores
    override suspend fun createChore(chore: Chore): Result<Unit, DataError> {
        createdChores.add(chore)
        chores.value = chores.value + chore
        return Result.Success(Unit)
    }
    override suspend fun updateChoreStatus(familyId: String, choreId: String, newStatus: ChoreStatus, adminId: String?): Result<Unit, DataError> = Result.Success(Unit)
    override suspend fun syncChores(familyId: String): Result<Unit, DataError> = Result.Success(Unit)
}

class FakeConnectivityRepository : ConnectivityRepository {
    override val isServerReachable = MutableStateFlow(true)
    override suspend fun checkHealth(): Result<Unit, DataError.Network> = Result.Success(Unit)
}

class FakeTokenStorage : org.aals.family.chore.core.domain.repository.TokenStorage {
    override val token = MutableStateFlow<String?>(null)
    override val familyId = MutableStateFlow<String?>(null)
    override val userId = MutableStateFlow<String?>(null)
    override val isOfflineMode = MutableStateFlow<Boolean?>(null)
    override val serverUrl = MutableStateFlow<String?>(null)
    override val serverName = MutableStateFlow<String?>(null)
    override val language = MutableStateFlow<String?>(null)

    override suspend fun setOfflineMode(enabled: Boolean?) { this.isOfflineMode.value = enabled }
    override suspend fun getOfflineMode(): Boolean? = isOfflineMode.value
    override suspend fun saveToken(token: String) { this.token.value = token }
    override suspend fun getToken(): String? = token.value
    override suspend fun saveFamilyId(familyId: String) { this.familyId.value = familyId }
    override suspend fun getFamilyId(): String? = familyId.value
    override suspend fun saveUserId(userId: String) { this.userId.value = userId }
    override suspend fun getUserId(): String? = userId.value
    override suspend fun saveServerUrl(url: String) { this.serverUrl.value = url }
    override suspend fun getServerUrl(): String? = serverUrl.value
    override suspend fun saveServerName(name: String) { this.serverName.value = name }
    override suspend fun getServerName(): String? = serverName.value
    override suspend fun saveLanguage(languageCode: String) { this.language.value = languageCode }
    override suspend fun getLanguage(): String? = language.value
    override suspend fun clear() {
        token.value = null
        familyId.value = null
        userId.value = null
        serverUrl.value = null
        serverName.value = null
        language.value = null
    }
    override suspend fun clearAuth() {
        token.value = null
        userId.value = null
    }
}
