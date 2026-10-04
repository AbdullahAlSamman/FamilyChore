package org.aals.family.chore.core.presentation.permissions

import androidx.compose.runtime.Composable

/**
 * A controller for handling camera permissions.
 */
interface CameraPermissionManager {
    /**
     * Request camera permission. The [onResult] callback will be invoked with `true` if granted, `false` otherwise.
     */
    fun requestPermission(onResult: (Boolean) -> Unit)
}

/**
 * Creates and remembers a [CameraPermissionManager] for the current platform.
 */
@Composable
expect fun rememberCameraPermissionManager(): CameraPermissionManager
