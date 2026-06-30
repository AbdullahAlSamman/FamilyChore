package org.aals.family.chore.feature.auth.presentation.qr_scanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun QrScannerView(
    onQrCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
)
