package org.aals.family.chore.feature.auth.presentation.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(
            state = WelcomeState(),
            onAction = {}
        )
    }
}

@Composable
fun WelcomeRoot(
    onNavigateToSetupFamily: () -> Unit,
    onNavigateToJoinFamily: () -> Unit,
    viewModel: WelcomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            WelcomeEvent.NavigateToSetupFamily -> onNavigateToSetupFamily()
            WelcomeEvent.NavigateToJoinFamily -> onNavigateToJoinFamily()
        }
    }

    WelcomeScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun WelcomeScreen(
    state: WelcomeState,
    onAction: (WelcomeAction) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to FamilyChore",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Get started by setting up your family hub or joining an existing one.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { onAction(WelcomeAction.OnSetupNewFamilyClick) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Setup New Family")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { onAction(WelcomeAction.OnJoinFamilyClick) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join Existing Family")
            }
        }
    }
}
