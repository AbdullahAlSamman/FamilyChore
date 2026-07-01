package org.aals.family.chore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.feature.auth.presentation.navigation.*
import co.touchlab.kermit.Logger
import org.koin.compose.koinInject

@Serializable object MainDashboardRoute

@Composable
@Preview
fun App(
    tokenStorage: TokenStorage = koinInject(),
    connectivityRepository: ConnectivityRepository = koinInject()
) {
    val authStartDestination by produceState<Any?>(initialValue = null) {
        val serverUrl = tokenStorage.getServerUrl()
        val familyId = tokenStorage.getFamilyId()

        value = if (serverUrl != null) {
            val result = connectivityRepository.checkHealth()
            if (result is Result.Success) {
                if (familyId != null) {
                    UserSelectionRoute(familyId = familyId)
                } else {
                    WelcomeRoute
                }
            } else {
                ServerDiscoveryRoute(isErrorMode = true)
            }
        } else {
            ServerDiscoveryRoute()
        }
    }

    val scope = rememberCoroutineScope()

    MaterialTheme {
        if (authStartDestination != null) {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = AuthGraph
            ) {
                authGraph(
                    navController = navController,
                    startDestination = authStartDestination!!,
                    onOnboardingComplete = {
                        scope.launch {
                            val familyId = tokenStorage.getFamilyId()
                            Logger.d { "Onboarding complete, returning to User Selection for family: $familyId" }
                            if (familyId != null) {
                                navController.navigate(UserSelectionRoute(familyId = familyId)) {
                                    popUpTo(AuthGraph) { inclusive = true }
                                }
                            } else {
                                navController.navigate(WelcomeRoute) {
                                    popUpTo(AuthGraph) { inclusive = true }
                                }
                            }
                        }
                    }
                )

                composable<MainDashboardRoute> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Main Dashboard Placeholder")
                        Button(onClick = {
                            navController.navigate(AuthGraph) {
                                popUpTo(MainDashboardRoute) { inclusive = true }
                            }
                        }) {
                            Text("Logout (Test)")
                        }
                    }
                }
            }
        }
    }
}
