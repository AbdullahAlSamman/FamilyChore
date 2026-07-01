package org.aals.family.chore.core.data.repository

import org.aals.family.chore.core.data.remote.PairingDataSource
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.map
import co.touchlab.kermit.Logger

class AuthRepositoryImpl(
    private val pairingDataSource: PairingDataSource,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun createFamily(familyName: String, parentNickname: String): Result<User, DataError.Network> {
        Logger.d { "Creating family: $familyName with parent: $parentNickname" }
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        
        return pairingDataSource.createFamily(serverUrl, familyName, parentNickname).map { response ->
            Logger.d { "Family created successfully: ${response.familyId}" }
            tokenStorage.saveFamilyId(response.familyId)
            tokenStorage.saveToken(response.token)
            response.parentUser
        }
    }

    override suspend fun generatePairingToken(familyId: String): Result<String, DataError.Network> {
        Logger.d { "Generating pairing token for family: $familyId" }
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        
        return pairingDataSource.generatePairingToken(serverUrl, familyId).map { it.pairingToken }
    }

    override suspend fun getPairingUsers(pairingToken: String): Result<List<User>, DataError.Network> {
        Logger.d { "Fetching users for pairing token" }
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        
        return pairingDataSource.getPairingUsers(serverUrl, pairingToken).map { it.users }
    }

    override suspend fun getFamilyUsers(familyId: String): Result<List<User>, DataError.Network> {
        Logger.d { "Fetching users for family: $familyId" }
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)

        return pairingDataSource.getFamilyUsers(serverUrl, familyId).map { it.users }
    }

    override suspend fun confirmPairing(pairingToken: String, userId: String): Result<User, DataError.Network> {
        Logger.d { "Confirming pairing for user: $userId" }
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        
        return pairingDataSource.confirmPairing(serverUrl, pairingToken, userId).map { response ->
            Logger.d { "Pairing confirmed for family: ${response.familyId}" }
            tokenStorage.saveFamilyId(response.familyId)
            tokenStorage.saveToken(response.token)
            response.user
        }
    }

    override suspend fun setupPin(userId: String, pin: String): Result<Unit, DataError.Network> {
        Logger.d { "Setting up PIN for user: $userId" }
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        return pairingDataSource.setupPin(serverUrl, userId, pin)
    }

    override suspend fun verifyPin(userId: String, pin: String): Result<Unit, DataError.Network> {
        Logger.d { "Verifying PIN for user: $userId" }
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        return pairingDataSource.verifyPin(serverUrl, userId, pin)
    }
}
