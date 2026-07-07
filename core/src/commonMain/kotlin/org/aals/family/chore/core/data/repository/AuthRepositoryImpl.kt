package org.aals.family.chore.core.data.repository

import co.touchlab.kermit.Logger
import org.aals.family.chore.core.data.remote.PairingDataSource
import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.map
import org.aals.family.chore.core.domain.util.onSuccess

class AuthRepositoryImpl(
    private val pairingDataSource: PairingDataSource,
    private val tokenStorage: TokenStorage,
    private val logger: Logger
) : AuthRepository {

    override suspend fun createFamily(familyName: String, parentNickname: String): Result<User, DataError.Network> {
        logger.d { "Creating family: $familyName with parent: $parentNickname" }
        return pairingDataSource.createFamily(familyName, parentNickname).map { response ->
            logger.d { "Family created successfully: ${response.familyId}" }
            tokenStorage.saveFamilyId(response.familyId)
            tokenStorage.saveUserId(response.parentUser.id)
            tokenStorage.saveToken(response.token)
            response.parentUser
        }
    }

    override suspend fun getFamilies(): Result<List<Family>, DataError.Network> {
        logger.d { "Fetching all families" }
        return pairingDataSource.getFamilies()
    }

    override suspend fun generatePairingToken(familyId: String): Result<String, DataError.Network> {
        logger.d { "Generating pairing token for family: $familyId" }
        return pairingDataSource.generatePairingToken(familyId).map { it.pairingToken }
    }

    override suspend fun getPairingUsers(pairingToken: String): Result<List<User>, DataError.Network> {
        logger.d { "Fetching users for pairing token" }
        return pairingDataSource.getPairingUsers(pairingToken).map { it.users }
    }

    override suspend fun getFamilyUsers(familyId: String): Result<List<User>, DataError.Network> {
        logger.d { "Fetching users for family: $familyId" }
        return pairingDataSource.getFamilyUsers(familyId).map { it.users }
    }

    override suspend fun confirmPairing(pairingToken: String, userId: String): Result<User, DataError.Network> {
        logger.d { "Confirming pairing for user: $userId" }
        return pairingDataSource.confirmPairing(pairingToken, userId).map { response ->
            logger.d { "Pairing confirmed for family: ${response.familyId}" }
            tokenStorage.saveFamilyId(response.familyId)
            tokenStorage.saveUserId(response.user.id)
            tokenStorage.saveToken(response.token)
            response.user
        }
    }

    override suspend fun setupPin(userId: String, pin: String): Result<Unit, DataError.Network> {
        logger.d { "Setting up PIN for user: $userId" }
        return pairingDataSource.setupPin(userId, pin)
    }

    override suspend fun verifyPin(userId: String, pin: String): Result<Unit, DataError.Network> {
        logger.d { "Verifying PIN for user: $userId" }
        return pairingDataSource.verifyPin(userId, pin).onSuccess {
            tokenStorage.saveUserId(userId)
        }
    }

    override suspend fun getCurrentUser(): Result<User, DataError.Network> {
        val userId = tokenStorage.getUserId() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        logger.d { "Fetching current user profile: $userId" }
        return pairingDataSource.getUser(userId)
    }
}
