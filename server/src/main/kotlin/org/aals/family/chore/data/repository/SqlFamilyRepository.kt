package org.aals.family.chore.data.repository

import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.data.local.*
import org.aals.family.chore.data.local.DatabaseFactory.dbQuery
import org.aals.family.chore.domain.repository.FamilyRepository
import co.touchlab.kermit.Logger
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class SqlFamilyRepository : FamilyRepository {

    override suspend fun createFamily(name: String): Family = dbQuery {
        Logger.d { "Creating family in DB: $name" }
        val familyId = UUID.randomUUID().toString()
        FamiliesTable.insert {
            it[id] = familyId
            it[FamiliesTable.name] = name
        }
        Family(familyId, name)
    }

    override suspend fun getFamily(id: String): Family? = dbQuery {
        FamiliesTable.selectAll().where { FamiliesTable.id eq id }
            .map { it.toFamily() }
            .singleOrNull()
    }

    override suspend fun addUserToFamily(familyId: String, nickname: String, role: UserRole): User = dbQuery {
        Logger.d { "Adding user $nickname ($role) to family $familyId" }
        val userId = UUID.randomUUID().toString()
        UsersTable.insert {
            it[id] = userId
            it[UsersTable.familyId] = familyId
            it[UsersTable.nickname] = nickname
            it[UsersTable.role] = role.name
            it[points] = 0
        }
        User(userId, familyId, nickname, role, 0)
    }

    override suspend fun getUsersInFamily(familyId: String): List<User> = dbQuery {
        UsersTable.selectAll().where { UsersTable.familyId eq familyId }
            .map { it.toUser() }
    }

    override suspend fun getUser(id: String): User? = dbQuery {
        UsersTable.selectAll().where { UsersTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun setPin(userId: String, pin: String): Unit = dbQuery {
        Logger.d { "Updating PIN for user: $userId" }
        val exists = PinsTable.selectAll().where { PinsTable.userId eq userId }.any()
        if (exists) {
            PinsTable.update({ PinsTable.userId eq userId }) {
                it[PinsTable.pin] = pin
            }
        } else {
            PinsTable.insert {
                it[PinsTable.userId] = userId
                it[PinsTable.pin] = pin
            }
        }
    }

    override suspend fun verifyPin(userId: String, pin: String): Boolean = dbQuery {
        PinsTable.selectAll().where { (PinsTable.userId eq userId) and (PinsTable.pin eq pin) }
            .any()
    }

    private fun ResultRow.toFamily() = Family(
        id = this[FamiliesTable.id],
        name = this[FamiliesTable.name]
    )

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id],
        familyId = this[UsersTable.familyId],
        nickname = this[UsersTable.nickname],
        role = UserRole.valueOf(this[UsersTable.role]),
        points = this[UsersTable.points]
    )
}
