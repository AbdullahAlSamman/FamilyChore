package org.aals.family.chore.feature.auth.di

import org.aals.family.chore.feature.auth.presentation.create_family.CreateFamilyViewModel
import org.aals.family.chore.feature.auth.presentation.discovery.ServerDiscoveryViewModel
import org.aals.family.chore.feature.auth.presentation.pin_entry.PinEntryViewModel
import org.aals.family.chore.feature.auth.presentation.qr_scanner.QrScannerViewModel
import org.aals.family.chore.feature.auth.presentation.user_selection.UserSelectionViewModel
import org.aals.family.chore.feature.auth.presentation.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::QrScannerViewModel)
    viewModelOf(::UserSelectionViewModel)
    viewModelOf(::PinEntryViewModel)
    viewModelOf(::CreateFamilyViewModel)
    viewModelOf(::ServerDiscoveryViewModel)
}
