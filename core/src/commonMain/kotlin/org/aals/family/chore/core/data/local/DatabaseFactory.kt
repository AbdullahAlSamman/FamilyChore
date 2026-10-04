package org.aals.family.chore.core.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

object DatabaseFactory {
    fun create(builder: RoomDatabase.Builder<FamilyDatabase>): FamilyDatabase {
        return builder
            // TODO: Remove destructive migration for production
            .fallbackToDestructiveMigration(true)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
