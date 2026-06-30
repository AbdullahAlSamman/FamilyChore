package org.aals.family.chore.feature.auth.presentation.welcome

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.aals.family.chore.core.domain.discovery.DiscoveredServer
import org.aals.family.chore.core.domain.discovery.ServerDiscovery

class FakeServerDiscovery : ServerDiscovery {
    private val _discoveryFlow = MutableSharedFlow<DiscoveredServer>()
    
    override fun startDiscovery(): Flow<DiscoveredServer> = _discoveryFlow
    
    override fun stopDiscovery() {}
    
    suspend fun emit(server: DiscoveredServer) {
        _discoveryFlow.emit(server)
    }
}
