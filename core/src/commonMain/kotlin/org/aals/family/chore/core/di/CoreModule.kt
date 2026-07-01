package org.aals.family.chore.core.di

import org.aals.family.chore.core.data.remote.HttpClientFactory
import org.aals.family.chore.core.data.remote.PairingDataSource
import org.aals.family.chore.core.data.remote.ServerHealthDataSource
import org.aals.family.chore.core.data.repository.AuthRepositoryImpl
import org.aals.family.chore.core.data.repository.ConnectivityRepositoryImpl
import org.aals.family.chore.core.data.repository.DataStoreTokenStorage
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    includes(platformModule)
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { HttpClientFactory.create(get()) }
    singleOf(::PairingDataSource)
    singleOf(::ServerHealthDataSource)
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::ConnectivityRepositoryImpl) { bind<ConnectivityRepository>() }
    singleOf(::DataStoreTokenStorage) { bind<TokenStorage>() }
}
