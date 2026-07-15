package org.aals.family.chore.feature.dashboard.di

import org.aals.family.chore.feature.dashboard.presentation.DashboardViewModel
import org.aals.family.chore.feature.dashboard.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule = module {
    viewModelOf(::DashboardViewModel)
    viewModelOf(::SettingsViewModel)
}
