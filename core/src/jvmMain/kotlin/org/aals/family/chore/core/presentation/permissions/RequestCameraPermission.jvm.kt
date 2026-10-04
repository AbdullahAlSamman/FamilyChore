package org.aals.family.chore.core.presentation.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCameraPermissionManager(): CameraPermissionManager {
    return remember {
        object : CameraPermissionManager {
            override fun requestPermission(onResult: (Boolean) -> Unit) {
                // Desktop generally doesn't have runtime camera permissions in the same way
                // as mobile.
                onResult(true)
            }
        }
    }
}
