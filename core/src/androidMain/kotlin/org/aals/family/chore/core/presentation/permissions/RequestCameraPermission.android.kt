package org.aals.family.chore.core.presentation.permissions

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCameraPermissionManager(): CameraPermissionManager {
    var onResultCallback: ((Boolean) -> Unit)? = null

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            onResultCallback?.invoke(granted)
            onResultCallback = null
        }
    )

    return remember(launcher) {
        object : CameraPermissionManager {
            override fun requestPermission(onResult: (Boolean) -> Unit) {
                onResultCallback = onResult
                launcher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
