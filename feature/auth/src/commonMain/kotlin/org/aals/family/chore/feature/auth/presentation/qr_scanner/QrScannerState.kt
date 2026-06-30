package org.aals.family.chore.feature.auth.presentation.qr_scanner

data class QrScannerState(
    val isLoading: Boolean = false,
    val error: String? = null
)
