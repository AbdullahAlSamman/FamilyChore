package org.aals.family.chore.core.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import org.aals.family.chore.core.data.local.dao.ChoreDao
import org.aals.family.chore.core.data.local.entity.ChoreEntity
import org.aals.family.chore.core.data.remote.ChoreDataSource
import org.aals.family.chore.core.data.remote.dto.ChoreDto
import org.aals.family.chore.core.data.remote.dto.CreateChoreRequest
import org.aals.family.chore.core.data.remote.dto.UpdateChoreStatusRequest
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

    override suspend fun updateChoreStatus(
        familyId: String,
        choreId: String,
        newStatus: ChoreStatus,
        adminId: String?
    ): Result<Unit, DataError> {
        logger.d { "Updating chore status: $choreId to $newStatus" }

        // Optimistic update
        val currentChore = choreDao.getChoreById(choreId, familyId)
        if (currentChore != null) {
            choreDao.upsertChore(currentChore.copy(status = newStatus.name))
        }

        val request = UpdateChoreStatusRequest(
            choreId = choreId,
            familyId = familyId,
            newStatus = newStatus,
            adminId = adminId
        )

        return choreDataSource.updateChoreStatus(request).map { Unit }
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
