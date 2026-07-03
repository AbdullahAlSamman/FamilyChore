package org.aals.family.chore.feature.dashboard.di

import org.aals.family.chore.feature.dashboard.presentation.DashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val dashboardModule = module {
    viewModel {
        DashboardViewModel(
            authRepository = get(),
            transactionRepository = get(),
            connectivityRepository = get(),
            logger = get { parametersOf("DashboardViewModel") }
        )
    }
}
