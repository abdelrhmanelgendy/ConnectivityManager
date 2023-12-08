package com.example.connectivitymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.connectivitymanager.ui.theme.ConnectivityManagerTheme
import net.sbs.internetchecker.data.InternetConnectivityChecker
import net.sbs.internetchecker.data.NetworkConnectivityObserver
import net.sbs.internetchecker.domain.ConnectivityObserver

class MainActivity : ComponentActivity() {
    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectivityObserver = NetworkConnectivityObserver(
            this, applicationContext,
            500000, InternetConnectivityChecker()
        )

        setContent {
            ConnectivityManagerTheme {
                val status by connectivityObserver.observe().collectAsState(
                    initial = ConnectivityObserver.Status.Available
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Network status: $status")
                }
            }
        }
    }
}