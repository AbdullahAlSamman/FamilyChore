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
import kotlinx.serialization.Serializable
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.feature.auth.presentation.navigation.AuthGraph
import org.aals.family.chore.feature.auth.presentation.navigation.ServerDiscoveryRoute
import org.aals.family.chore.feature.auth.presentation.navigation.WelcomeRoute
import org.aals.family.chore.feature.auth.presentation.navigation.authGraph
import org.koin.compose.koinInject

@Serializable object MainDashboardRoute

@Composable
@Preview
fun App(
    tokenStorage: TokenStorage = koinInject()
) {
    val navState by produceState<Pair<Any, Boolean>?>(initialValue = null) {
        val token = tokenStorage.getToken()
        val familyId = tokenStorage.getFamilyId()
        val serverUrl = tokenStorage.getServerUrl()

        value = when {
            token != null && familyId != null && serverUrl != null -> MainDashboardRoute to false
            serverUrl != null -> AuthGraph to true
            else -> AuthGraph to false
        }
    }

    MaterialTheme {
        if (navState != null) {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = navState!!.first
            ) {
                authGraph(
                    navController = navController,
                    startAtWelcome = navState!!.second,
                    onOnboardingComplete = {
                        navController.navigate(MainDashboardRoute) {
                            popUpTo(AuthGraph) { inclusive = true }
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
                            Text("Go to Auth (Testing)")
                        }
                    }
                }
            }
        }
    }
}
