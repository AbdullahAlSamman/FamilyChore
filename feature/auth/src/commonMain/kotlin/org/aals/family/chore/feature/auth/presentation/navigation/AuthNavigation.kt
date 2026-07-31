package org.aals.family.chore.feature.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import org.aals.family.chore.feature.auth.presentation.create_family.CreateFamilyRoot
import org.aals.family.chore.feature.auth.presentation.discovery.ServerDiscoveryRoot
import org.aals.family.chore.feature.auth.presentation.mode_selection.ModeSelectionRoot
import org.aals.family.chore.feature.auth.presentation.pin_entry.PinEntryRoot
import org.aals.family.chore.feature.auth.presentation.qr_scanner.QrScannerRoot
import org.aals.family.chore.feature.auth.presentation.user_selection.UserSelectionRoot
import org.aals.family.chore.feature.auth.presentation.welcome.WelcomeRoot

@Serializable object AuthGraph

@Serializable object ModeSelectionRoute
@Serializable data class ServerDiscoveryRoute(val isErrorMode: Boolean = false)
@Serializable object WelcomeRoute
@Serializable object CreateFamilyRoute
@Serializable object QrScannerRoute
@Serializable data class UserSelectionRoute(
    val pairingToken: String? = null,
    val familyId: String? = null,
    val isFromOnboarding: Boolean = false
)
@Serializable data class PinEntryRoute(val userId: String, val isSetupMode: Boolean = false)

/**
 * Defines the navigation graph for the authentication and onboarding flow.
 * 
 * @param navController The navigation controller to use for transitions.
 * @param startDestination The initial destination within the auth graph. Defaults to ServerDiscoveryRoute.
 * @param onOnboardingComplete Callback triggered when the onboarding/auth flow is finished.
 */
fun NavGraphBuilder.authGraph(
    navController: NavController,
    startDestination: Any = ModeSelectionRoute,
    onOnboardingComplete: () -> Unit
) {
    navigation<AuthGraph>(
        startDestination = startDestination
    ) {
        composable<ModeSelectionRoute> {
            ModeSelectionRoot(
                onNavigateToWelcome = {
                    navController.navigate(WelcomeRoute)
                },
                onNavigateToDiscovery = {
                    navController.navigate(ServerDiscoveryRoute())
                }
            )
        }
        composable<ServerDiscoveryRoute> {
            ServerDiscoveryRoot(
                onNavigateToWelcome = {
                    navController.navigate(WelcomeRoute)
                },
                onNavigateBack = {
                    navController.popBackStack()
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
                },
                onNavigateToUserSelection = { familyId ->
                    navController.navigate(UserSelectionRoute(familyId = familyId, isFromOnboarding = true))
                },
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(ModeSelectionRoute) {
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable<CreateFamilyRoute> {
            CreateFamilyRoot(
                onNavigateBack = { navController.popBackStack() },
                onFamilyCreated = { _, userId ->
                    navController.navigate(PinEntryRoute(userId, isSetupMode = true))
                }
            )
        }
        composable<QrScannerRoute> {
            QrScannerRoot(
                onNavigateBack = { navController.popBackStack() },
                onQrCodeDetected = { _, _ ->
                    onOnboardingComplete()
                }
            )
        }
        composable<UserSelectionRoute> {
            UserSelectionRoot(
                onPairingConfirmed = { userId ->
                    navController.navigate(PinEntryRoute(userId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<PinEntryRoute> {
            PinEntryRoot(
                onPinVerified = onOnboardingComplete,
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(ModeSelectionRoute) {
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
