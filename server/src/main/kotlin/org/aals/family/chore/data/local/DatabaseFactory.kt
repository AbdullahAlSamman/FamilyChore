package org.aals.family.chore.data.local

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {
    fun init() {
        val databasePath = "data"
        val directory = File(databasePath)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:$databasePath/familychore.db"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_SERIALIZABLE"
            validate()
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(FamiliesTable, UsersTable, PinsTable, TransactionsTable, RewardsTable, ChoresTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

object FamiliesTable : Table("families") {
    val id = varchar("id", 50)
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}

object UsersTable : Table("users") {
    val id = varchar("id", 50)
    val familyId = varchar("family_id", 50) references FamiliesTable.id
    val nickname = varchar("nickname", 100)
    val role = varchar("role", 20)
    val points = integer("points").default(0)
    val requiresPin = bool("requires_pin").default(true)

    override val primaryKey = PrimaryKey(id)
}

object PinsTable : Table("pins") {
    val userId = varchar("user_id", 50) references UsersTable.id
    val pin = varchar("pin", 64)

    override val primaryKey = PrimaryKey(userId)
}

object TransactionsTable : Table("transactions") {
    val id = varchar("id", 50)
    val familyId = varchar("family_id", 50) references FamiliesTable.id
    val userId = varchar("user_id", 50) references UsersTable.id
    val adminId = varchar("admin_id", 50).nullable()
    val amount = integer("amount")
    val type = varchar("type", 20)
    val timestamp = long("timestamp")
    val note = varchar("note", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

object RewardsTable : Table("rewards") {
    val id = varchar("id", 50)
    val familyId = varchar("family_id", 50) references FamiliesTable.id
    val title = varchar("title", 100)
    val description = varchar("description", 255)
    val pointCost = integer("point_cost")

    override val primaryKey = PrimaryKey(id)
}

object ChoresTable : Table("chores") {
    val id = varchar("id", 50)
    val familyId = varchar("family_id", 50) references FamiliesTable.id
    val name = varchar("name", 100)
    val description = varchar("description", 255).nullable()
    val points = integer("points")
    val status = varchar("status", 20)
    val assignedTo = varchar("assigned_to", 50) references UsersTable.id
    val createdBy = varchar("created_by", 50) references UsersTable.id
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}
