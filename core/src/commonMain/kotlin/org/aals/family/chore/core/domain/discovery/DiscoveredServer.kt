package org.aals.family.chore.core.domain.discovery

import kotlinx.serialization.Serializable

@Serializable
data class DiscoveredServer(
    val name: String,
    val url: String
)
