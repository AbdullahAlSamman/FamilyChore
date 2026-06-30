package org.aals.family.chore.di

import org.aals.family.chore.core.di.coreModule
import org.aals.family.chore.feature.auth.di.authModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(coreModule, authModule)
    }
}
