package org.aals.family.chore.feature.auth.di

import org.aals.family.chore.feature.auth.presentation.create_family.CreateFamilyViewModel
import org.aals.family.chore.feature.auth.presentation.discovery.ServerDiscoveryViewModel
import org.aals.family.chore.feature.auth.presentation.pin_entry.PinEntryViewModel
import org.aals.family.chore.feature.auth.presentation.qr_scanner.QrScannerViewModel
import org.aals.family.chore.feature.auth.presentation.user_selection.UserSelectionViewModel
import org.aals.family.chore.feature.auth.presentation.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val authModule = module {
    viewModel { WelcomeViewModel(get(), get { parametersOf("WelcomeViewModel") }) }
    viewModel { QrScannerViewModel(get(), get { parametersOf("QrScannerViewModel") }) }
    viewModel { UserSelectionViewModel(get(), get(), get { parametersOf("UserSelectionViewModel") }) }
    viewModel { PinEntryViewModel(get(), get(), get { parametersOf("PinEntryViewModel") }) }
    viewModel { CreateFamilyViewModel(get(), get { parametersOf("CreateFamilyViewModel") }) }
    viewModel { ServerDiscoveryViewModel(get(), get(), get(), get { parametersOf("ServerDiscoveryViewModel") }) }
}
