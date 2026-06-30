package org.aals.family.chore.core.di

import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import org.aals.family.chore.core.data.local.DataStoreFactory
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
}
