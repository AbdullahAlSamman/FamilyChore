package org.aals.family.chore.core.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.aals.family.chore.core.data.remote.BaseUrlProvider
import org.aals.family.chore.core.data.remote.HttpClientFactory
import org.aals.family.chore.core.data.remote.KtorServerHealthDataSource
import org.aals.family.chore.core.data.remote.PairingDataSource
import org.aals.family.chore.core.data.remote.ServerHealthDataSource
import org.aals.family.chore.core.data.remote.TokenStorageBaseUrlProvider
import org.aals.family.chore.core.data.remote.TransactionDataSource
import org.aals.family.chore.core.data.repository.AuthRepositoryImpl
import org.aals.family.chore.core.data.repository.ConnectivityRepositoryImpl
import org.aals.family.chore.core.data.repository.DataStoreTokenStorage
import org.aals.family.chore.core.data.repository.TransactionRepositoryImpl
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.repository.TransactionRepository
import org.aals.family.chore.core.domain.util.LoggingInitializer
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val coreModule = module {
    includes(platformModule)
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { HttpClientFactory.create(get(), get(), get { parametersOf("HttpClient") }) }
    factory { (tag: String) -> LoggingInitializer.createLogger(tag) }
    singleOf(::TokenStorageBaseUrlProvider) { bind<BaseUrlProvider>() }
    singleOf(::PairingDataSource)
    singleOf(::TransactionDataSource)
    singleOf(::KtorServerHealthDataSource) { bind<ServerHealthDataSource>() }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get { parametersOf("AuthRepository") }) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get(), get { parametersOf("TransactionRepository") }) }
    singleOf(::ConnectivityRepositoryImpl) { bind<ConnectivityRepository>() }
    singleOf(::DataStoreTokenStorage) { bind<TokenStorage>() }
}
