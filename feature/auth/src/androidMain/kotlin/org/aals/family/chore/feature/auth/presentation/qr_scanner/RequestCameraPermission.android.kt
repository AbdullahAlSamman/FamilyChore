package org.aals.family.chore.feature.auth.presentation.qr_scanner

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun RequestCameraPermission(
    trigger: Any,
    onResult: (Boolean) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )
    LaunchedEffect(trigger) {
        launcher.launch(Manifest.permission.CAMERA)
    }
}
