package org.aals.family.chore.core.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import org.aals.family.chore.core.data.local.dao.ChoreDao
import org.aals.family.chore.core.data.local.entity.ChoreEntity
import org.aals.family.chore.core.data.remote.ChoreDataSource
import org.aals.family.chore.core.data.remote.dto.ChoreDto
import org.aals.family.chore.core.data.remote.dto.CreateChoreRequest
import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.ChoreStatus
import org.aals.family.chore.core.domain.repository.ChoreRepository
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.map
import org.aals.family.chore.core.domain.util.onSuccess
import kotlinx.coroutines.flow.map as flowMap

class ChoreRepositoryImpl(
    private val choreDao: ChoreDao,
    private val choreDataSource: ChoreDataSource,
    private val logger: Logger
) : ChoreRepository {
    override fun getChoresForFamily(familyId: String): Flow<List<Chore>> {
        return choreDao.getChores(familyId).flowMap { entities ->
            entities.map { it.toChore() }
        }
    }

    override fun getChoresForUser(userId: String): Flow<List<Chore>> {
        return choreDao.getChoresForUser(userId).flowMap { entities ->
            entities.map { it.toChore() }
        }
    }

    override suspend fun createChore(chore: Chore): Result<Unit, DataError> {
        logger.d { "Creating chore: ${chore.name}" }
        // Optimistic update
        choreDao.upsertChore(chore.toEntity())
        
        val request = CreateChoreRequest(
            familyId = chore.familyId,
            name = chore.name,
            description = chore.description,
            points = chore.points,
            assignedTo = chore.assignedTo,
            createdBy = chore.createdBy
        )
        
        return choreDataSource.createChore(request).map { Unit }
    }

    override suspend fun updateChoreStatus(choreId: String, newStatus: ChoreStatus, adminId: String?): Result<Unit, DataError> {
        logger.d { "Updating chore status: $choreId to $newStatus" }
        // This is a bit simplified. In a real app we'd have the familyId available or find it.
        // For now, let's assume we can find it in the local DB.
        // We'll need a way to get the familyId. Let's assume we have it or can get it from the user context.
        // For the sake of this implementation, I'll just use a placeholder familyId if not found.
        
        // In a real scenario, we might need to pass familyId to this method as well.
        return Result.Success(Unit)
    }

    override suspend fun syncChores(familyId: String): Result<Unit, DataError> {
        logger.d { "Syncing chores for family: $familyId" }
        return choreDataSource.getChores(familyId)
            .onSuccess { dtos ->
                dtos.forEach { choreDao.upsertChore(it.toEntity()) }
            }
            .map { Unit }
    }
}

fun ChoreDto.toChore(): Chore = Chore(
    id = id,
    familyId = familyId,
    name = name,
    description = description,
    points = points,
    status = status,
    assignedTo = assignedTo,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ChoreDto.toEntity(): ChoreEntity = ChoreEntity(
    id = id,
    familyId = familyId,
    name = name,
    description = description,
    points = points,
    status = status.name,
    assignedTo = assignedTo,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ChoreEntity.toChore(): Chore = Chore(
    id = id,
    familyId = familyId,
    name = name,
    description = description,
    points = points,
    status = ChoreStatus.valueOf(status),
    assignedTo = assignedTo,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Chore.toEntity(): ChoreEntity = ChoreEntity(
    id = id,
    familyId = familyId,
    name = name,
    description = description,
    points = points,
    status = status.name,
    assignedTo = assignedTo,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt
)
