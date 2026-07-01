package org.aals.family.chore.feature.auth.presentation.discovery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.discovery.DiscoveredServer
import org.aals.family.chore.core.domain.discovery.ServerDiscovery
import org.aals.family.chore.core.domain.repository.TokenStorage
import co.touchlab.kermit.Logger

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
}

sealed interface ServerDiscoveryEvent {
    data object NavigateToWelcome : ServerDiscoveryEvent
}

class ServerDiscoveryViewModel(
    private val serverDiscovery: ServerDiscovery,
    private val tokenStorage: TokenStorage,
    private val savedStateHandle: SavedStateHandle
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
        Logger.d { "Starting server discovery scan" }
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
            is ServerDiscoveryAction.OnServerSelected -> {
                Logger.d { "Server selected: ${action.server.name} (${action.server.url})" }
                viewModelScope.launch {
                    tokenStorage.saveServerUrl(action.server.url)
                    tokenStorage.saveServerName(action.server.name)
                    _events.send(ServerDiscoveryEvent.NavigateToWelcome)
                }
            }
            ServerDiscoveryAction.OnScanAgainClick -> {
                Logger.d { "Rescanning for servers" }
                serverDiscovery.stopDiscovery()
                startScanning()
            }
            is ServerDiscoveryAction.OnManualUrlChange -> {
                _state.update { it.copy(manualUrl = action.url) }
            }
            ServerDiscoveryAction.OnConnectManualClick -> {
                Logger.d { "Connecting manually to: ${state.value.manualUrl}" }
                viewModelScope.launch {
                    tokenStorage.saveServerUrl(state.value.manualUrl)
                    tokenStorage.saveServerName("Manual Server")
                    _events.send(ServerDiscoveryEvent.NavigateToWelcome)
                }
            }
        }
    }
}
