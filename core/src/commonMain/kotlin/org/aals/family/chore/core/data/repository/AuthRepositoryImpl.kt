package org.aals.family.chore.core.data.repository

import org.aals.family.chore.core.data.remote.PairingDataSource
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.map

class AuthRepositoryImpl(
    private val pairingDataSource: PairingDataSource,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun createFamily(familyName: String, parentNickname: String): Result<User, DataError.Network> {
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        
        return pairingDataSource.createFamily(serverUrl, familyName, parentNickname).map { response ->
            tokenStorage.saveFamilyId(response.familyId)
            tokenStorage.saveToken(response.token)
            response.parentUser
        }
    }

    override suspend fun generatePairingToken(familyId: String): Result<String, DataError.Network> {
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        
        return pairingDataSource.generatePairingToken(serverUrl, familyId).map { it.pairingToken }
    }

    override suspend fun getPairingUsers(pairingToken: String): Result<List<User>, DataError.Network> {
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        
        return pairingDataSource.getPairingUsers(serverUrl, pairingToken).map { it.users }
    }

    override suspend fun confirmPairing(pairingToken: String, userId: String): Result<User, DataError.Network> {
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        
        return pairingDataSource.confirmPairing(serverUrl, pairingToken, userId).map { response ->
            tokenStorage.saveFamilyId(response.familyId)
            tokenStorage.saveToken(response.token)
            response.user
        }
    }

    override suspend fun setupPin(userId: String, pin: String): Result<Unit, DataError.Network> {
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        return pairingDataSource.setupPin(serverUrl, userId, pin)
    }

    override suspend fun verifyPin(userId: String, pin: String): Result<Unit, DataError.Network> {
        val serverUrl = tokenStorage.getServerUrl() ?: return Result.Error(DataError.Network.UNKNOWN)
        return pairingDataSource.verifyPin(serverUrl, userId, pin)
    }
}
