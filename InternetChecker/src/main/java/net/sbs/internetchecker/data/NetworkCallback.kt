package net.sbs.internetchecker.data

import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.launch
import net.sbs.internetchecker.domain.ConnectivityObserver

internal object NetworkCallback {

    fun createNetworkCallback(
        flow: ProducerScope<ConnectivityObserver.Status>,
        onHardwareAvailable: suspend () -> Unit,
        onHardwareUnAvailable: suspend () -> Unit,
    ): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                println("onAvailable")
                flow.launch {
                    onHardwareAvailable()
                }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                println("onLosing")

                flow.launch {
                    flow.send(ConnectivityObserver.Status.Losing)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                println("onLost")
                flow.launch {
                    onHardwareUnAvailable()
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                println("onUnavailable")

                flow.launch {
                    onHardwareUnAvailable()
                }
            }
        }
    }
}