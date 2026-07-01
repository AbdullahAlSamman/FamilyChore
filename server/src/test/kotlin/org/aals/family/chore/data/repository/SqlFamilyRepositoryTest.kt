package org.aals.family.chore.data.repository

import kotlinx.coroutines.runBlocking
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.data.local.DatabaseFactory
import org.aals.family.chore.data.local.FamiliesTable
import org.aals.family.chore.data.local.UsersTable
import org.aals.family.chore.data.local.PinsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*
import java.io.File

class SqlFamilyRepositoryTest {

    private lateinit var repository: SqlFamilyRepository
    private val testDbPath = "data_test"

    @BeforeTest
    fun setUp() {
        val directory = File(testDbPath)
        if (directory.exists()) {
            directory.deleteRecursively()
        }
        directory.mkdirs()

        Database.connect(
            url = "jdbc:sqlite:$testDbPath/test.db",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            SchemaUtils.create(FamiliesTable, UsersTable, PinsTable)
        }
        repository = SqlFamilyRepository()
    }

    @AfterTest
    fun tearDown() {
        File(testDbPath).deleteRecursively()
    }

    @Test
    fun `create and retrieve family`() = runBlocking {
        val family = repository.createFamily("Smiths")
        val retrieved = repository.getFamily(family.id)
        
        assertEquals(family, retrieved)
        assertEquals("Smiths", retrieved?.name)
    }

    @Test
    fun `add and retrieve user`() = runBlocking {
        val family = repository.createFamily("Smiths")
        val user = repository.addUserToFamily(family.id, "John", UserRole.PARENT)
        
        val retrieved = repository.getUser(user.id)
        val familyUsers = repository.getUsersInFamily(family.id)

        assertEquals(user, retrieved)
        assertTrue(familyUsers.contains(user))
    }

    @Test
    fun `set and verify pin`() = runBlocking {
        val family = repository.createFamily("Smiths")
        val user = repository.addUserToFamily(family.id, "John", UserRole.PARENT)
        
        repository.setPin(user.id, "1234")
        
        assertTrue(repository.verifyPin(user.id, "1234"))
        assertFalse(repository.verifyPin(user.id, "0000"))
    }
}
