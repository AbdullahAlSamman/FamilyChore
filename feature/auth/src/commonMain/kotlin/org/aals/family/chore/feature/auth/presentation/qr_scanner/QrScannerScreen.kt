package org.aals.family.chore.feature.auth.presentation.qr_scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    if (!state.hasCameraPermission) {
        RequestCameraPermission(
            trigger = state.permissionRequestCount,
            onResult = { granted ->
                viewModel.onAction(QrScannerAction.OnPermissionResult(granted))
            }
        )
    }

    QrScannerScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
expect fun RequestCameraPermission(
    trigger: Any,
    onResult: (Boolean) -> Unit
)

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
            if (state.hasCameraPermission) {
                QrScannerView(
                    onQrCodeScanned = { content ->
                        onAction(QrScannerAction.OnQrCodeScanned(content))
                    },
                    modifier = Modifier
                        .size(250.dp)
                        .background(Color.Black)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission required", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(onClick = { onAction(QrScannerAction.OnRetryPermissionClick) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.White)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Point your camera at the QR code on the parent device.")
            
            state.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
