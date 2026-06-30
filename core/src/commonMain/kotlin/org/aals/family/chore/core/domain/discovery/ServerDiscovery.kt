package org.aals.family.chore.core.domain.discovery

import kotlinx.coroutines.flow.Flow

interface ServerDiscovery {
    fun startDiscovery(): Flow<DiscoveredServer>
    fun stopDiscovery()
}
