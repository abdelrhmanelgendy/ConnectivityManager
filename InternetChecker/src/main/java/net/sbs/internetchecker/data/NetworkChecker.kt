package net.sbs.internetchecker.data

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

internal class NetworkChecker(private val connectivityManager: ConnectivityManager) {
    fun hasValidConnection(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }
}