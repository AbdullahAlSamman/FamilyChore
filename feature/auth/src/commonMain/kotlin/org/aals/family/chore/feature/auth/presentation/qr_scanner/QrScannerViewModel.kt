package org.aals.family.chore.feature.auth.presentation.qr_scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.aals.family.chore.core.domain.model.PairingToken
import org.aals.family.chore.core.domain.repository.TokenStorage

class QrScannerViewModel(
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _state = MutableStateFlow(QrScannerState())
    val state = _state.asStateFlow()

    private val _events = Channel<QrScannerEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: QrScannerAction) {
        when (action) {
            is QrScannerAction.OnQrCodeScanned -> {
                parseQrContent(action.content)
            }
            QrScannerAction.OnBackClick -> {
                viewModelScope.launch {
                    _events.send(QrScannerEvent.NavigateBack)
                }
            }
            is QrScannerAction.OnPermissionResult -> {
                _state.value = _state.value.copy(hasCameraPermission = action.granted)
            }
            QrScannerAction.OnRetryPermissionClick -> {
                _state.value = _state.value.copy(
                    permissionRequestCount = _state.value.permissionRequestCount + 1
                )
            }
        }
    }

    private fun parseQrContent(content: String) {
        try {
            val pairingToken = Json.decodeFromString<PairingToken>(content)
            viewModelScope.launch {
                tokenStorage.saveServerUrl(pairingToken.serverIp)
                _events.send(QrScannerEvent.QrCodeDetected(pairingToken.serverIp, pairingToken.token))
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Invalid QR code")
        }
    }
}
