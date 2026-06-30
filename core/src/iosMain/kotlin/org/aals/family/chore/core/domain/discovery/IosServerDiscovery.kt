package org.aals.family.chore.core.domain.discovery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class IosServerDiscovery : ServerDiscovery {
    override fun startDiscovery(): Flow<DiscoveredServer> = emptyFlow()
    override fun stopDiscovery() {}
}
