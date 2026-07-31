package org.aals.family.chore.core.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.aals.family.chore.core.data.local.dao.ChoreDao
import org.aals.family.chore.core.data.local.dao.FamilyDao
import org.aals.family.chore.core.data.local.dao.TransactionDao
import org.aals.family.chore.core.data.local.dao.UserDao
import org.aals.family.chore.core.data.local.entity.ChoreEntity
import org.aals.family.chore.core.data.local.entity.FamilyEntity
import org.aals.family.chore.core.data.local.entity.TransactionEntity
import org.aals.family.chore.core.data.local.entity.UserEntity

@Database(
    entities = [ChoreEntity::class, UserEntity::class, TransactionEntity::class, FamilyEntity::class],
    version = 4
)
@ConstructedBy(FamilyDatabaseConstructor::class)
abstract class FamilyDatabase : RoomDatabase() {
    abstract fun choreDao(): ChoreDao
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun familyDao(): FamilyDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object FamilyDatabaseConstructor : RoomDatabaseConstructor<FamilyDatabase> {
    override fun initialize(): FamilyDatabase
}
