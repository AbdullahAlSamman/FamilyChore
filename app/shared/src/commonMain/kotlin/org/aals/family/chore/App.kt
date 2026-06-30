package org.aals.family.chore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.navigation.compose.rememberNavController
import org.aals.family.chore.feature.auth.presentation.navigation.AuthGraph
import org.aals.family.chore.feature.auth.presentation.navigation.authGraph

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = AuthGraph
        ) {
            authGraph(
                navController = navController,
                onOnboardingComplete = {
                    // Navigate to Main dashboard
                }
            )
        }
    }
}
