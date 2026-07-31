package org.aals.family.chore.feature.auth.presentation

import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

class FakeAuthRepository : AuthRepository {
    var users = mutableListOf<User>()
    var families = mutableListOf<Family>()
    var pairingToken = "default_token"
    var error: DataError.Network? = null

    override suspend fun createFamily(familyName: String, parentNickname: String): Result<User, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(User("1", "family1", parentNickname, org.aals.family.chore.core.domain.model.UserRole.PARENT, 0))
    }

    override suspend fun getFamilies(): Result<List<Family>, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(families)
    }

    override suspend fun generatePairingToken(familyId: String): Result<String, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(pairingToken)
    }

    override suspend fun getPairingUsers(pairingToken: String): Result<List<User>, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(users)
    }

    override suspend fun getFamilyMembers(familyId: String): Result<List<User>, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(users)
    }

    override suspend fun confirmPairing(pairingToken: String, userId: String): Result<User, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(users.find { it.id == userId } ?: User(userId, "family1", "User", org.aals.family.chore.core.domain.model.UserRole.CHILD, 0))
    }

    override suspend fun setupPin(userId: String, pin: String): Result<Unit, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(Unit)
    }

    override suspend fun verifyPin(userId: String, pin: String): Result<Unit, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(Unit)
    }

    override suspend fun addFamilyMember(
        familyId: String,
        nickname: String,
        role: UserRole,
        pin: String?,
        requiresPin: Boolean
    ): Result<User, DataError.Network> {
        val user = User("new", familyId, nickname, role, 0, requiresPin)
        users.add(user)
        return error?.let { Result.Error(it) } ?: Result.Success(user)
    }

    override suspend fun updateUserPinRequirement(
        userId: String,
        requiresPin: Boolean
    ): Result<Unit, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(Unit)
    }

    override suspend fun getUser(userId: String): Result<User, DataError.Network> {
        val user = users.find { it.id == userId } ?: User(userId, "family1", "User", org.aals.family.chore.core.domain.model.UserRole.CHILD, 0)
        return error?.let { Result.Error(it) } ?: Result.Success(user)
    }

    override suspend fun selectUser(userId: String): Result<Unit, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(Unit)
    }

    override suspend fun getCurrentUser(): Result<User, DataError.Network> {
        return error?.let { Result.Error(it) } ?: Result.Success(users.firstOrNull() ?: User("1", "family1", "User", org.aals.family.chore.core.domain.model.UserRole.PARENT, 0))
    }
}
