package org.aals.family.chore

import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo
import java.net.InetAddress
import java.net.NetworkInterface

class DiscoveryBroadcaster {
    private var jmdns: JmDNS? = null

    fun start(port: Int) {
        try {
            val ipAddress = getLocalIpAddress()
            val hostName = InetAddress.getByName(ipAddress).hostName ?: "FamilyChoreServer"
            jmdns = JmDNS.create(InetAddress.getByName(ipAddress))

            val serviceInfo = ServiceInfo.create(
                "_familychore._tcp.local.",
                hostName,
                port,
                "FamilyChore server discovery"
            )

            jmdns?.registerService(serviceInfo)
            println("Broadcasting FamilyChore server ($hostName) on $ipAddress:$port")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLocalIpAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address.isLoopbackAddress) continue
                    if (address is java.net.Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
            "127.0.0.1"
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }

    fun stop() {
        jmdns?.unregisterAllServices()
        jmdns?.close()
    }
}
