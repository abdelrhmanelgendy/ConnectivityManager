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
import kotlinx.coroutines.flow.Flow
import net.sbs.internetchecker.data.InternetConnectivityChecker
import net.sbs.internetchecker.data.ConnectivityObserver
import net.sbs.internetchecker.domain.IConnectivityObserver

class MainActivity : ComponentActivity() {
    private lateinit var connectivityObserver: Flow<IConnectivityObserver.Status>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectivityObserver = ConnectivityObserver(
            this, applicationContext,
            2000, InternetConnectivityChecker()
        ).observe()

        setContent {
            ConnectivityManagerTheme {
                val status by connectivityObserver.collectAsState(
                    initial = IConnectivityObserver.Status.Available
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