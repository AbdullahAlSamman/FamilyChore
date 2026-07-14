package org.aals.family.chore

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.model.AppLanguage
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.feature.auth.presentation.navigation.AuthGraph
import org.aals.family.chore.feature.auth.presentation.navigation.ServerDiscoveryRoute
import org.aals.family.chore.feature.auth.presentation.navigation.UserSelectionRoute
import org.aals.family.chore.feature.auth.presentation.navigation.authGraph
import org.aals.family.chore.feature.dashboard.presentation.navigation.DashboardGraph
import org.aals.family.chore.feature.dashboard.presentation.navigation.dashboardGraph
import org.aals.family.chore.presentation.MainState
import org.aals.family.chore.presentation.MainViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
@Preview
fun App(
    viewModel: MainViewModel = koinViewModel(),
    tokenStorage: TokenStorage = koinInject(),
    logger: Logger = koinInject { parametersOf("App") }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    val language = (state as? MainState.Success)?.language ?: AppLanguage.ENGLISH
    val layoutDirection = if (language == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme {
            when (val currentState = state) {
                MainState.Loading -> {
                    // You might want to show a splash screen or loader here
                }
                is MainState.Success -> {
                    val authStartDestination = currentState.startDestination
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = if (authStartDestination is DashboardGraph) DashboardGraph else AuthGraph
                    ) {
                        authGraph(
                            navController = navController,
                            startDestination = if (authStartDestination is DashboardGraph) ServerDiscoveryRoute() else authStartDestination,
                            onOnboardingComplete = {
                                scope.launch {
                                    logger.d { "Onboarding complete, navigating to Dashboard" }
                                    navController.navigate(DashboardGraph) {
                                        popUpTo(AuthGraph) { inclusive = true }
                                    }
                                }
                            }
                        )

                        dashboardGraph(
                            navController = navController,
                            onLogout = { isServerOnline, familyId ->
                                scope.launch {
                                    logger.d { "Logging out (online: $isServerOnline)" }
                                    if (isServerOnline && familyId != null) {
                                        tokenStorage.clearAuth()
                                        navController.navigate(UserSelectionRoute(familyId = familyId)) {
                                            popUpTo(DashboardGraph) { inclusive = true }
                                        }
                                    } else {
                                        tokenStorage.clear()
                                        navController.navigate(AuthGraph) {
                                            popUpTo(DashboardGraph) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
