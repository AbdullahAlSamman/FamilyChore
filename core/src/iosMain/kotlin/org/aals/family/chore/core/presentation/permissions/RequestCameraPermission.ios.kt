package org.aals.family.chore.core.presentation.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCameraPermissionManager(): CameraPermissionManager {
    return remember {
        object : CameraPermissionManager {
            override fun requestPermission(onResult: (Boolean) -> Unit) {
                // In a real iOS app, you would use AVAuthorizationStatus and requestAccessForMediaType
                // For simplicity in this demo, we assume true or handle it elsewhere if needed natively.
                onResult(true)
            }
        }
    }
}
