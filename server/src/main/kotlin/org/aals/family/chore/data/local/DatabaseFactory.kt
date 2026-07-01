package org.aals.family.chore.data.local

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
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
            SchemaUtils.create(FamiliesTable, UsersTable, PinsTable)
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

    override val primaryKey = PrimaryKey(id)
}

object PinsTable : Table("pins") {
    val userId = varchar("user_id", 50) references UsersTable.id
    val pin = varchar("pin", 4)

    override val primaryKey = PrimaryKey(userId)
}
