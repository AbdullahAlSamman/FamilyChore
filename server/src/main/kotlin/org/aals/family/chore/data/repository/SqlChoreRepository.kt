package org.aals.family.chore.data.repository

import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.ChoreStatus
import org.aals.family.chore.data.local.ChoresTable
import org.aals.family.chore.data.local.DatabaseFactory.dbQuery
import org.aals.family.chore.domain.repository.ChoreRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class SqlChoreRepository : ChoreRepository {
    
    override suspend fun getChoresByFamily(familyId: String): List<Chore> = dbQuery {
        ChoresTable.selectAll().where { ChoresTable.familyId eq familyId }
            .map { it.toChore() }
    }

    override suspend fun getChoresByUser(userId: String): List<Chore> = dbQuery {
        ChoresTable.selectAll().where { ChoresTable.assignedTo eq userId }
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
            it[recurrence] = chore.recurrence.name
            it[activeDays] = chore.activeDays.joinToString(",")
            it[activeWindowStart] = chore.activeWindowStart
            it[activeWindowEnd] = chore.activeWindowEnd
            it[penalty] = chore.penalty
            it[requiresPhotoProof] = chore.requiresPhotoProof
            it[iconName] = chore.iconName
            it[parentChoreId] = chore.parentChoreId
            it[dueDate] = chore.dueDate
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
            updatedAt = this[ChoresTable.updatedAt],
            recurrence = org.aals.family.chore.core.domain.model.Recurrence.valueOf(this[ChoresTable.recurrence]),
            activeDays = this[ChoresTable.activeDays].split(",").mapNotNull { it.toIntOrNull() },
            activeWindowStart = this[ChoresTable.activeWindowStart],
            activeWindowEnd = this[ChoresTable.activeWindowEnd],
            penalty = this[ChoresTable.penalty],
            requiresPhotoProof = this[ChoresTable.requiresPhotoProof],
            iconName = this[ChoresTable.iconName],
            parentChoreId = this[ChoresTable.parentChoreId],
            dueDate = this[ChoresTable.dueDate]
        )
    }
}
