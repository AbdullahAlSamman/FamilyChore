package org.aals.family.chore.feature.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.aals.family.chore.feature.auth.presentation.create_family.CreateFamilyRoot
import org.aals.family.chore.feature.auth.presentation.discovery.ServerDiscoveryRoot
import org.aals.family.chore.feature.auth.presentation.pin_entry.PinEntryRoot
import org.aals.family.chore.feature.auth.presentation.qr_scanner.QrScannerRoot
import org.aals.family.chore.feature.auth.presentation.user_selection.UserSelectionRoot
import org.aals.family.chore.feature.auth.presentation.welcome.WelcomeRoot

@Serializable object AuthGraph

@Serializable object ServerDiscoveryRoute
@Serializable object WelcomeRoute
@Serializable object CreateFamilyRoute
@Serializable object QrScannerRoute
@Serializable data class UserSelectionRoute(val pairingToken: String)
@Serializable data class PinEntryRoute(val userId: String, val isSetupMode: Boolean = false)

fun NavGraphBuilder.authGraph(
    navController: NavController,
    startAtWelcome: Boolean = false,
    onOnboardingComplete: () -> Unit
) {
    navigation<AuthGraph>(
        startDestination = if (startAtWelcome) WelcomeRoute else ServerDiscoveryRoute
    ) {
        composable<ServerDiscoveryRoute> {
            ServerDiscoveryRoot(
                onNavigateToWelcome = {
                    navController.navigate(WelcomeRoute)
                }
            )
        }
        composable<WelcomeRoute> {
            WelcomeRoot(
                onNavigateToSetupFamily = {
                    navController.navigate(CreateFamilyRoute)
                },
                onNavigateToJoinFamily = {
                    navController.navigate(QrScannerRoute)
                }
            )
        }
        composable<CreateFamilyRoute> {
            CreateFamilyRoot(
                onNavigateBack = { navController.popBackStack() },
                onFamilyCreated = { familyId, userId ->
                    navController.navigate(PinEntryRoute(userId, isSetupMode = true))
                }
            )
        }
        composable<QrScannerRoute> {
            QrScannerRoot(
                onNavigateBack = { navController.popBackStack() },
                onQrCodeDetected = { _, token ->
                    navController.navigate(UserSelectionRoute(token))
                }
            )
        }
        composable<UserSelectionRoute> { backStackEntry ->
            val route: UserSelectionRoute = backStackEntry.toRoute()
            UserSelectionRoot(
                onPairingConfirmed = { userId ->
                    navController.navigate(PinEntryRoute(userId))
                }
            )
        }
        composable<PinEntryRoute> {
            PinEntryRoot(
                onPinVerified = onOnboardingComplete
            )
        }
    }
}
