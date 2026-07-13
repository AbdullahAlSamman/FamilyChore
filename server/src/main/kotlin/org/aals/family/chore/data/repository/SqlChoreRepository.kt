package org.aals.family.chore.data.repository

import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.ChoreStatus
import org.aals.family.chore.data.local.ChoresTable
import org.aals.family.chore.data.local.DatabaseFactory.dbQuery
import org.aals.family.chore.domain.repository.ChoreRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class SqlChoreRepository : ChoreRepository {
    
    override suspend fun getChoresByFamily(familyId: String): List<Chore> = dbQuery {
        ChoresTable.select { ChoresTable.familyId eq familyId }
            .map { it.toChore() }
    }

    override suspend fun getChoresByUser(userId: String): List<Chore> = dbQuery {
        ChoresTable.select { ChoresTable.assignedTo eq userId }
            .map { it.toChore() }
    }

    override suspend fun createChore(chore: Chore): Chore = dbQuery {
        ChoresTable.insert {
            it[id] = chore.id
            it[familyId] = chore.familyId
            it[name] = chore.name
            it[description] = chore.description
            it[points] = chore.points
            it[status] = chore.status.name
            it[assignedTo] = chore.assignedTo
            it[createdBy] = chore.createdBy
            it[createdAt] = chore.createdAt
            it[updatedAt] = chore.updatedAt
        }
        chore
    }

    override suspend fun updateChoreStatus(
        choreId: String,
        familyId: String,
        newStatus: ChoreStatus,
        adminId: String?
    ): Boolean = dbQuery {
        ChoresTable.update({ (ChoresTable.id eq choreId) and (ChoresTable.familyId eq familyId) }) {
            it[status] = newStatus.name
            it[updatedAt] = System.currentTimeMillis()
        } > 0
    }

    private fun ResultRow.toChore(): Chore {
        return Chore(
            id = this[ChoresTable.id],
            familyId = this[ChoresTable.familyId],
            name = this[ChoresTable.name],
            description = this[ChoresTable.description],
            points = this[ChoresTable.points],
            status = ChoreStatus.valueOf(this[ChoresTable.status]),
            assignedTo = this[ChoresTable.assignedTo],
            createdBy = this[ChoresTable.createdBy],
            createdAt = this[ChoresTable.createdAt],
            updatedAt = this[ChoresTable.updatedAt]
        )
    }
}
