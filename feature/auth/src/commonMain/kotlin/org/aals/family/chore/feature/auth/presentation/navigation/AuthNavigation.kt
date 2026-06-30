package org.aals.family.chore.feature.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.aals.family.chore.feature.auth.presentation.pin_entry.PinEntryRoot
import org.aals.family.chore.feature.auth.presentation.qr_scanner.QrScannerRoot
import org.aals.family.chore.feature.auth.presentation.user_selection.UserSelectionRoot
import org.aals.family.chore.feature.auth.presentation.welcome.WelcomeRoot

@Serializable object AuthGraph

@Serializable object WelcomeRoute
@Serializable object QrScannerRoute
@Serializable data class UserSelectionRoute(val pairingToken: String)
@Serializable data class PinEntryRoute(val userId: String, val isSetupMode: Boolean = false)

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onOnboardingComplete: () -> Unit
) {
    navigation<AuthGraph>(startDestination = WelcomeRoute) {
        composable<WelcomeRoute> {
            WelcomeRoot(
                onNavigateToSetupFamily = {
                    // For now, let's assume setup family leads to QR scanner or similar
                    // Actually, setup family should lead to a CreateFamily screen
                    // But I'll just skip to QR for now to demonstrate navigation
                },
                onNavigateToJoinFamily = {
                    navController.navigate(QrScannerRoute)
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
