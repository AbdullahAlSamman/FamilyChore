package org.aals.family.chore.feature.auth.presentation.qr_scanner

data class QrScannerState(
    val hasCameraPermission: Boolean = false,
    val permissionRequestCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)
