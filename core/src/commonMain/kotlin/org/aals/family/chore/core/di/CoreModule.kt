package org.aals.family.chore.core.di

import org.aals.family.chore.core.data.remote.HttpClientFactory
import org.aals.family.chore.core.data.remote.PairingDataSource
import org.aals.family.chore.core.data.repository.AuthRepositoryImpl
import org.aals.family.chore.core.data.repository.DataStoreTokenStorage
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    includes(platformModule)
    single { HttpClientFactory.create(get()) }
    singleOf(::PairingDataSource)
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::DataStoreTokenStorage) { bind<TokenStorage>() }
}
