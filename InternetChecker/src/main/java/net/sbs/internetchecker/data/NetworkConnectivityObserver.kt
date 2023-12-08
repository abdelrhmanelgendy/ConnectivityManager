package net.sbs.internetchecker.data

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.sbs.internetchecker.domain.ConnectivityObserver
import net.sbs.internetchecker.domain.ConnectivityObserver.Status
import net.sbs.internetchecker.domain.IInternetConnectivityChecker


class NetworkConnectivityObserver(
    private val lifecycleOwner: LifecycleOwner, context: Context,
    private val checkIntervalMillis: Long = 5000L, private val internetConnectivityChecker: IInternetConnectivityChecker,
    private val withLogger: Boolean = true
) : ConnectivityObserver {
    private var internetConnectivityJob: Job? = null
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<Status> {
        val hardwareAvailable = checkInternetConnectivity(connectivityManager)
        return callbackFlow {
            val callback = NetworkCallback.createNetworkCallback(this,
                onHardwareAvailable = {

                    if (internetConnectivityJob == null || internetConnectivityJob?.isActive == false) {
                        internetConnectivityJob = lifecycleOwner.lifecycleScope.launch {
                            send(Status.AvailableHardware)
                            checkConnectivity(this@callbackFlow)
                        }
                    }
                },
                onHardwareUnAvailable = {
                    send(Status.UnavailableHardware)
                    if (internetConnectivityJob != null) {
                        if (internetConnectivityJob!!.isActive) {
                            internetConnectivityJob?.cancel()
                            internetConnectivityJob=null
                        }
                    }
                }
            )
            if (hardwareAvailable) {
                if (internetConnectivityJob == null || internetConnectivityJob?.isActive == false) {
                    internetConnectivityJob = lifecycleOwner.lifecycleScope.launch {
                        send(Status.AvailableHardware)
                        checkConnectivity(this@callbackFlow)
                    }
                }
             } else {
                send(Status.UnavailableHardware)
                if (internetConnectivityJob != null) {

                    if (internetConnectivityJob!!.isActive) {
                        internetConnectivityJob?.cancel()
                    }
                }
            }

            val lifecycleObserver = createLifecycleObserver(this, callback)
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

            if (hardwareAvailable.not()) {
                send(if (hardwareAvailable) Status.AvailableHardware else Status.UnavailableHardware)
            }

            awaitClose {
                cleanup(callback)
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }

        }.distinctUntilChanged().onEach {
            logg("Current status: $it")
        }
    }

    private fun createLifecycleObserver(
        flow: ProducerScope<Status>,
        callback: ConnectivityManager.NetworkCallback
    ): DefaultLifecycleObserver {
        return object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                if (internetConnectivityJob == null || internetConnectivityJob?.isActive == false) {
                    internetConnectivityJob = owner.lifecycleScope.launch {
                        startPeriodicInternetConnectivityCheck(flow)
                    }
                }
                logg("On Resume...")
                connectivityManager.registerDefaultNetworkCallback(callback)
            }

            override fun onPause(owner: LifecycleOwner) {
                logg("On Pause...")
                connectivityManager.unregisterNetworkCallback(callback)
                internetConnectivityJob?.cancel()
            }
        }
    }

    private fun checkInternetConnectivity(
        connectivityManager: ConnectivityManager
    ): Boolean {
        return NetworkChecker(connectivityManager).hasValidConnection()
    }

    private suspend fun startPeriodicInternetConnectivityCheck(flow: ProducerScope<Status>) {
        while (true) {
            if(checkInternetConnectivity(connectivityManager).not()){
                delay(checkIntervalMillis)
                continue
            }
            val hasInternet = withContext(Dispatchers.IO) {
                logg("Calling periodic API...")
                internetConnectivityChecker.hasInternetAccess()
            }
            if (hasInternet) {
                flow.send(Status.Available)

            } else {
                flow.send(Status.Unavailable)
            }
            delay(checkIntervalMillis)
        }
    }
    private fun checkConnectivity(flow: ProducerScope<Status>) =flow.launch{
        val hasInternet = withContext(Dispatchers.IO) {
            logg("Calling API...")
            internetConnectivityChecker.hasInternetAccess()
        }
        if (hasInternet) {
            flow.send(Status.Available)

        } else {
            flow.send(Status.Unavailable)
        }
        startPeriodicInternetConnectivityCheck(flow)
    }

    private fun cleanup(callback: ConnectivityManager.NetworkCallback) {
        connectivityManager.unregisterNetworkCallback(callback)
        internetConnectivityJob?.cancel()
        logg("cleanup")
    }

    private fun logg(message: String) {
        if (withLogger) {
            println("NetworkConnectivityObserver:--  $message")
        }
    }
}
