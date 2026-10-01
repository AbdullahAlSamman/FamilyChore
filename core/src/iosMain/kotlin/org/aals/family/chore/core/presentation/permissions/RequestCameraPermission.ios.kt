package org.aals.family.chore.core.presentation.permissions

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
