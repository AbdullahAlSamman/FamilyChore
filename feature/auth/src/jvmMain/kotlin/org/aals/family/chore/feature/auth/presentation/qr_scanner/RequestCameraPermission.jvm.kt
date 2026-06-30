package org.aals.family.chore.feature.auth.presentation.qr_scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun RequestCameraPermission(
    trigger: Any,
    onResult: (Boolean) -> Unit
) {
    LaunchedEffect(trigger) {
        onResult(true)
    }
}
