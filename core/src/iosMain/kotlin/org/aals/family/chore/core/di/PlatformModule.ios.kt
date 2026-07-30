package org.aals.family.chore.core.di

import androidx.room.Room
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import org.aals.family.chore.core.data.local.DataStoreFactory
import org.aals.family.chore.core.data.local.DatabaseFactory
import org.aals.family.chore.core.data.local.FamilyDatabase
import org.aals.family.chore.core.data.local.FamilyDatabaseConstructor
import org.aals.family.chore.core.domain.discovery.IosServerDiscovery
import org.aals.family.chore.core.domain.discovery.ServerDiscovery
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single { Darwin.create() }
    singleOf(::IosServerDiscovery) { bind<ServerDiscovery>() }
    single {
        DataStoreFactory.create(
            producePath = {
                val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null
                )
                (requireNotNull(documentDirectory).path + "/${DataStoreFactory.DATASTORE_FILENAME}")
            }
        )
    }
    single {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        val path = requireNotNull(documentDirectory).path + "/familychore.db"
        val builder = Room.databaseBuilder<FamilyDatabase>(
            name = path,
            factory = { FamilyDatabaseConstructor.initialize() }
        )
        DatabaseFactory.create(builder)
    }
    single { get<FamilyDatabase>().userDao() }
    single { get<FamilyDatabase>().choreDao() }
    single { get<FamilyDatabase>().transactionDao() }
    single { get<FamilyDatabase>().familyDao() }
}
