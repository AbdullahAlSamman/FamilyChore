package org.aals.family.chore.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardRoot(
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    DashboardScreen(
        onLogout = onLogout
    )
}

@Composable
fun DashboardScreen(
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Main Dashboard (Feature Module)")
        Button(onClick = onLogout) {
            Text("Logout (Test)")
        }
    }
}
