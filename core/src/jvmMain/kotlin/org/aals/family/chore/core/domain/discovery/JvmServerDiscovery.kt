package org.aals.family.chore.core.domain.discovery

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import java.net.InetAddress

class JvmServerDiscovery : ServerDiscovery {
    private var jmdns: JmDNS? = null

    override fun startDiscovery(): Flow<DiscoveredServer> = callbackFlow {
        val jmdns = try {
            JmDNS.create(InetAddress.getLocalHost())
        } catch (e: Exception) {
            JmDNS.create()
        }
        this@JvmServerDiscovery.jmdns = jmdns

        val listener = object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                jmdns.requestServiceInfo(event.type, event.name)
            }

            override fun serviceRemoved(event: ServiceEvent) {}

            override fun serviceResolved(event: ServiceEvent) {
                val info = event.info
                val name = event.name
                val port = info.port
                val host = info.inetAddresses.firstOrNull()?.hostAddress ?: info.hostAddresses.firstOrNull()
                
                if (host != null) {
                    trySend(DiscoveredServer(name = name, url = "http://$host:$port"))
                }
            }
        }

        jmdns.addServiceListener("_familychore._tcp.local.", listener)

        awaitClose {
            stopDiscovery()
        }
    }

    override fun stopDiscovery() {
        jmdns?.close()
        jmdns = null
    }
}
