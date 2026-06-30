package org.aals.family.chore.feature.auth.presentation.qr_scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun QrScannerView(
    onQrCodeScanned: (String) -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("QR Scanner not supported on JVM", color = Color.White)
    }
}
