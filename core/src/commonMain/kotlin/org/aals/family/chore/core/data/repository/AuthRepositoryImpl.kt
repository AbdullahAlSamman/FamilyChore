package org.aals.family.chore.core.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import org.aals.family.chore.core.data.local.dao.FamilyDao
import org.aals.family.chore.core.data.local.dao.UserDao
import org.aals.family.chore.core.data.local.entity.FamilyEntity
import org.aals.family.chore.core.data.local.entity.UserEntity
import org.aals.family.chore.core.data.remote.PairingDataSource
import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.map
import org.aals.family.chore.core.domain.util.onSuccess
import org.aals.family.chore.core.domain.util.randomUUID
import org.aals.family.chore.core.domain.util.toSha256
import kotlinx.coroutines.flow.map as flowMap

class AuthRepositoryImpl(
    private val pairingDataSource: PairingDataSource,
    private val tokenStorage: TokenStorage,
    private val userDao: UserDao,
    private val familyDao: FamilyDao,
    private val logger: Logger
) : AuthRepository {

    override suspend fun createFamily(familyName: String, parentNickname: String): Result<User, DataError.Network> {
        logger.d { "Creating family: $familyName with parent: $parentNickname" }
        
        if (tokenStorage.getOfflineMode() == true) {
            val familyId = randomUUID()
            val userId = randomUUID()
            val user = User(
                id = userId,
                familyId = familyId,
                nickname = parentNickname,
                role = UserRole.PARENT,
                requiresPin = false
            )
            
            familyDao.upsertFamily(FamilyEntity(id = familyId, name = familyName))
            userDao.upsertUser(user.toEntity())
            
            tokenStorage.saveFamilyId(familyId)
            tokenStorage.saveUserId(userId)
            
            return Result.Success(user)
        }

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
        if (tokenStorage.getOfflineMode() == true) {
            return familyDao.getFamilies()
                .flowMap { entities -> entities.map { it.toDomain() } }
                .first().let { Result.Success(it) }
        }
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

    override suspend fun getFamilyMembers(familyId: String): Result<List<User>, DataError.Network> {
        logger.d { "Fetching members for family: $familyId" }
        if (tokenStorage.getOfflineMode() == true) {
            return userDao.getUsers(familyId)
                .flowMap { entities -> entities.map { it.toUser() } }
                .first().let { Result.Success(it) }
        }
        return pairingDataSource.getFamilyMembers(familyId).map { it.users }
    }

    override suspend fun addChildUser(
        familyId: String,
        nickname: String,
        requiresPin: Boolean
    ): Result<User, DataError.Network> {
        logger.d { "Adding child user: $nickname to family: $familyId" }
        return pairingDataSource.addChildUser(familyId, nickname, requiresPin)
    }

    override suspend fun updateUserPinRequirement(
        userId: String,
        requiresPin: Boolean
    ): Result<Unit, DataError.Network> {
        logger.d { "Updating pin requirement for user: $userId to $requiresPin" }
        return pairingDataSource.updateUserPinRequirement(userId, requiresPin)
    }

    override suspend fun getUser(userId: String): Result<User, DataError.Network> {
        logger.d { "Fetching user profile: $userId" }
        if (tokenStorage.getOfflineMode() == true) {
            return userDao.getUserById(userId)?.toUser()
                ?.let { Result.Success(it) } ?: Result.Error(DataError.Network.NOT_FOUND)
        }
        return pairingDataSource.getUser(userId)
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
        val hashedPin = pin.toSha256()
        logger.d { "Setting up PIN for user: $userId (offline: ${tokenStorage.getOfflineMode()})" }
        
        if (tokenStorage.getOfflineMode() == true) {
            val userEntity = userDao.getUserById(userId) ?: return Result.Error(DataError.Network.NOT_FOUND)
            val updatedUser = userEntity.copy(pin = hashedPin)
            userDao.upsertUser(updatedUser)
            tokenStorage.saveUserId(userId)
            return Result.Success(Unit)
        }
        
        return pairingDataSource.setupPin(userId, hashedPin).onSuccess {
            tokenStorage.saveUserId(userId)
        }
    }

    override suspend fun verifyPin(userId: String, pin: String): Result<Unit, DataError.Network> {
        val hashedPin = pin.toSha256()
        logger.d { "Verifying PIN for user: $userId (offline: ${tokenStorage.getOfflineMode()})" }
        
        if (tokenStorage.getOfflineMode() == true) {
            val userEntity = userDao.getUserById(userId) ?: return Result.Error(DataError.Network.NOT_FOUND)
            return if (userEntity.pin == hashedPin) {
                tokenStorage.saveUserId(userId)
                Result.Success(Unit)
            } else {
                Result.Error(DataError.Network.UNAUTHORIZED)
            }
        }
        
        return pairingDataSource.verifyPin(userId, hashedPin).onSuccess {
            tokenStorage.saveUserId(userId)
        }
    }

    override suspend fun getCurrentUser(): Result<User, DataError.Network> {
        val userId = tokenStorage.getUserId() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        logger.d { "Fetching current user profile: $userId" }
        if (tokenStorage.getOfflineMode() == true) {
            return getUser(userId)
        }
        return pairingDataSource.getUser(userId)
    }
}

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    familyId = familyId,
    name = nickname,
    role = role.name,
    pin = null
)

fun UserEntity.toUser(): User = User(
    id = id,
    familyId = familyId,
    nickname = name,
    role = UserRole.valueOf(role),
    requiresPin = pin != null
)

fun FamilyEntity.toDomain(): Family = Family(
    id = id,
    name = name
)
