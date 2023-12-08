package net.sbs.internetchecker.data

import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.launch
import net.sbs.internetchecker.domain.IConnectivityObserver

internal object NetworkCallback {
    fun createNetworkCallback(
        flow: ProducerScope<IConnectivityObserver.Status>,
        onHardwareAvailable: suspend () -> Unit,
        onHardwareUnAvailable: suspend () -> Unit,
    ): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                flow.launch {
                    onHardwareAvailable()
                }
            }
            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                flow.launch {
                    flow.send(IConnectivityObserver.Status.Losing)
                }
            }
            override fun onLost(network: Network) {
                super.onLost(network)
                flow.launch {
                    onHardwareUnAvailable()
                }
            }
            override fun onUnavailable() {
                super.onUnavailable()
                flow.launch {
                    onHardwareUnAvailable()
                }
            }
        }
    }
}