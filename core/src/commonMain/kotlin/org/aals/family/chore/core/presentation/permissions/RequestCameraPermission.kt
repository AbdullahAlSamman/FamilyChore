package org.aals.family.chore.core.presentation.permissions

import androidx.compose.runtime.Composable

@Composable
expect fun RequestCameraPermission(
    trigger: Any,
    onResult: (Boolean) -> Unit
)
