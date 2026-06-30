package org.aals.family.chore.core.di

import io.ktor.client.engine.okhttp.OkHttp
import org.aals.family.chore.core.data.local.DataStoreFactory
import org.aals.family.chore.core.domain.discovery.AndroidServerDiscovery
import org.aals.family.chore.core.domain.discovery.ServerDiscovery
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    single { OkHttp.create() }
    singleOf(::AndroidServerDiscovery) { bind<ServerDiscovery>() }
    single {
        DataStoreFactory.create(
            producePath = {
                File(androidContext().filesDir, "datastore/${DataStoreFactory.DATASTORE_FILENAME}").absolutePath
            }
        )
    }
}
