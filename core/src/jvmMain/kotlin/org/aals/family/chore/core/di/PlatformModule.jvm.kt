package org.aals.family.chore.core.di

import io.ktor.client.engine.okhttp.OkHttp
import org.aals.family.chore.core.data.local.DataStoreFactory
import org.aals.family.chore.core.domain.discovery.JvmServerDiscovery
import org.aals.family.chore.core.domain.discovery.ServerDiscovery
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    single { OkHttp.create() }
    singleOf(::JvmServerDiscovery) { bind<ServerDiscovery>() }
    single {
        DataStoreFactory.create(
            producePath = {
                File(System.getProperty("user.home"), ".familychore/${DataStoreFactory.DATASTORE_FILENAME}").absolutePath
            }
        )
    }
}
