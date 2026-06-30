package org.aals.family.chore.feature.auth.presentation.qr_scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.aals.family.chore.core.presentation.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QrScannerRoot(
    onNavigateBack: () -> Unit,
    onQrCodeDetected: (String, String) -> Unit,
    viewModel: QrScannerViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is QrScannerEvent.QrCodeDetected -> onQrCodeDetected(event.serverUrl, event.pairingToken)
            QrScannerEvent.NavigateBack -> onNavigateBack()
        }
    }

    QrScannerScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    state: QrScannerState,
    onAction: (QrScannerAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                navigationIcon = {
                    IconButton(onClick = { onAction(QrScannerAction.OnBackClick) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text("Camera Preview Placeholder", color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Point your camera at the QR code on the parent device.")
            
            state.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Dummy button to simulate a scan for now
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { 
                // In a real app, this would be triggered by the camera/ML Kit
                onAction(QrScannerAction.OnQrCodeScanned("{\"token\":\"dummy-token\",\"serverIp\":\"http://192.168.1.100:8080\"}"))
            }) {
                Text("Simulate Scan")
            }
        }
    }
}
