package org.aals.family.chore.core.domain.repository

import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

interface AuthRepository {
    suspend fun createFamily(familyName: String, parentNickname: String): Result<User, DataError.Network>
    suspend fun getFamilies(): Result<List<Family>, DataError.Network>
    suspend fun generatePairingToken(familyId: String): Result<String, DataError.Network>
    suspend fun getPairingUsers(pairingToken: String): Result<List<User>, DataError.Network>
    suspend fun getFamilyMembers(familyId: String): Result<List<User>, DataError.Network>
    suspend fun confirmPairing(pairingToken: String, userId: String): Result<User, DataError.Network>
    suspend fun setupPin(userId: String, pin: String): Result<Unit, DataError.Network>
    suspend fun verifyPin(userId: String, pin: String): Result<Unit, DataError.Network>
    suspend fun addFamilyMember(
        familyId: String,
        nickname: String,
        role: UserRole,
        pin: String? = null,
        requiresPin: Boolean = true
    ): Result<User, DataError.Network>
    suspend fun updateUserPinRequirement(userId: String, requiresPin: Boolean): Result<Unit, DataError.Network>
    suspend fun getUser(userId: String): Result<User, DataError.Network>
    suspend fun selectUser(userId: String): Result<Unit, DataError.Network>
    suspend fun getCurrentUser(): Result<User, DataError.Network>
}
