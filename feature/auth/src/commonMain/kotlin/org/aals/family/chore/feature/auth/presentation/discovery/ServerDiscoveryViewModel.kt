package org.aals.family.chore.feature.auth.presentation.discovery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.discovery.DiscoveredServer
import org.aals.family.chore.core.domain.discovery.ServerDiscovery
import org.aals.family.chore.core.domain.repository.TokenStorage

data class ServerDiscoveryState(
    val discoveredServers: List<DiscoveredServer> = emptyList(),
    val isScanning: Boolean = false,
    val manualUrl: String = "http://10.0.2.2:8080",
    val isErrorMode: Boolean = false
)

sealed interface ServerDiscoveryAction {
    data class OnServerSelected(val server: DiscoveredServer) : ServerDiscoveryAction
    data object OnScanAgainClick : ServerDiscoveryAction
    data class OnManualUrlChange(val url: String) : ServerDiscoveryAction
    data object OnConnectManualClick : ServerDiscoveryAction
    data object OnBackClick : ServerDiscoveryAction
}

sealed interface ServerDiscoveryEvent {
    data object NavigateToWelcome : ServerDiscoveryEvent
    data object NavigateBack : ServerDiscoveryEvent
}

class ServerDiscoveryViewModel(
    private val serverDiscovery: ServerDiscovery,
    private val tokenStorage: TokenStorage,
    private val savedStateHandle: SavedStateHandle,
    private val logger: Logger
) : ViewModel() {

    private val _state = MutableStateFlow(
        ServerDiscoveryState(
            isErrorMode = savedStateHandle["isErrorMode"] ?: false
        )
    )
    val state = _state.asStateFlow()

    private val _events = Channel<ServerDiscoveryEvent>()
    val events = _events.receiveAsFlow()

    init {
        startScanning()
    }

    private fun startScanning() {
        logger.d { "Starting server discovery scan" }
        _state.update { it.copy(isScanning = true, discoveredServers = emptyList()) }
        serverDiscovery.startDiscovery()
            .onEach { server ->
                _state.update { state ->
                    if (state.discoveredServers.any { it.url == server.url }) {
                        state
                    } else {
                        state.copy(discoveredServers = state.discoveredServers + server)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ServerDiscoveryAction) {
        when (action) {
            ServerDiscoveryAction.OnBackClick -> {
                viewModelScope.launch {
                    _events.send(ServerDiscoveryEvent.NavigateBack)
                }
            }
            is ServerDiscoveryAction.OnServerSelected -> {
                logger.d { "Server selected: ${action.server.name} (${action.server.url})" }
                viewModelScope.launch {
                    tokenStorage.saveServerUrl(action.server.url)
                    tokenStorage.saveServerName(action.server.name)
                    _events.send(ServerDiscoveryEvent.NavigateToWelcome)
                }
            }
            ServerDiscoveryAction.OnScanAgainClick -> {
                logger.d { "Rescanning for servers" }
                serverDiscovery.stopDiscovery()
                startScanning()
            }
            is ServerDiscoveryAction.OnManualUrlChange -> {
                _state.update { it.copy(manualUrl = action.url) }
            }
            ServerDiscoveryAction.OnConnectManualClick -> {
                logger.d { "Connecting manually to: ${state.value.manualUrl}" }
                viewModelScope.launch {
                    tokenStorage.saveServerUrl(state.value.manualUrl)
                    tokenStorage.saveServerName("Manual Server")
                    _events.send(ServerDiscoveryEvent.NavigateToWelcome)
                }
            }
        }
    }
}
