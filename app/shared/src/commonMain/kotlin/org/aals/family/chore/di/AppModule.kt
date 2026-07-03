package org.aals.family.chore.di

import org.aals.family.chore.presentation.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MainViewModel)
}
