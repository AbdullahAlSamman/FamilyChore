package org.aals.family.chore.core.domain.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidServerDiscovery(
    context: Context
) : ServerDiscovery {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    override fun startDiscovery(): Flow<DiscoveredServer> = callbackFlow {
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onDiscoveryStarted(serviceType: String?) {}

            override fun onDiscoveryStopped(serviceType: String?) {}

            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                if (serviceInfo?.serviceType == "_familychore._tcp.") {
                    nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {}

                        override fun onServiceResolved(resolvedInfo: NsdServiceInfo?) {
                            val host = resolvedInfo?.host?.hostAddress
                            val port = resolvedInfo?.port
                            val name = resolvedInfo?.serviceName ?: "Unknown Server"
                            if (host != null && port != null) {
                                trySend(DiscoveredServer(name = name, url = "http://$host:$port"))
                            }
                        }
                    })
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {}
        }

        nsdManager.discoverServices("_familychore._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        awaitClose {
            stopDiscovery()
        }
    }

    override fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                Logger.e(e) { "Error stopping service discovery" }
            }
        }
        discoveryListener = null
    }
}
