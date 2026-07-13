package org.aals.family.chore.core.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.aals.family.chore.core.data.remote.BaseUrlProvider
import org.aals.family.chore.core.data.remote.ChoreDataSource
import org.aals.family.chore.core.data.remote.HttpClientFactory
import org.aals.family.chore.core.data.remote.KtorServerHealthDataSource
import org.aals.family.chore.core.data.remote.PairingDataSource
import org.aals.family.chore.core.data.remote.ServerHealthDataSource
import org.aals.family.chore.core.data.remote.TokenStorageBaseUrlProvider
import org.aals.family.chore.core.data.remote.TransactionDataSource
import org.aals.family.chore.core.data.repository.AuthRepositoryImpl
import org.aals.family.chore.core.data.repository.ChoreRepositoryImpl
import org.aals.family.chore.core.data.repository.ConnectivityRepositoryImpl
import org.aals.family.chore.core.data.repository.DataStoreTokenStorage
import org.aals.family.chore.core.data.repository.TransactionRepositoryImpl
import org.aals.family.chore.core.data.util.DefaultTimeProvider
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.ChoreRepository
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.repository.TransactionRepository
import org.aals.family.chore.core.domain.util.LoggingInitializer
import org.aals.family.chore.core.domain.util.TimeProvider
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val coreModule = module {
    includes(platformModule)
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    singleOf(::DefaultTimeProvider) { bind<TimeProvider>() }
    single { HttpClientFactory.create(get(), get(), get(), get { parametersOf("HttpClient") }) }
    factory { params -> LoggingInitializer.createLogger(params.getOrNull<String>() ?: "General") }
    singleOf(::TokenStorageBaseUrlProvider) { bind<BaseUrlProvider>() }
    singleOf(::PairingDataSource)
    singleOf(::TransactionDataSource)
    singleOf(::ChoreDataSource)
    singleOf(::KtorServerHealthDataSource) { bind<ServerHealthDataSource>() }
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::TransactionRepositoryImpl) { bind<TransactionRepository>() }
    singleOf(::ChoreRepositoryImpl) { bind<ChoreRepository>() }
    singleOf(::ConnectivityRepositoryImpl) { bind<ConnectivityRepository>() }
    singleOf(::DataStoreTokenStorage) { bind<TokenStorage>() }
}
