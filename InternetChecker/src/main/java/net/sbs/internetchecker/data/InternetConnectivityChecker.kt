package net.sbs.internetchecker.data

import net.sbs.internetchecker.domain.IInternetConnectivityChecker
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class InternetConnectivityChecker(
    override val baseUrl: String = DEFAULT_BASE_URL,
    override val reqMethod: String = DEFAULT_REQ_METHOD,
    override val connectionTimeOut: Long = DEFAULT_CONNECTION_TIMEOUT,
    override val requestProps: Map<String, String>? = DEFAULT_REQUEST_PROPS
) : IInternetConnectivityChecker {

    override fun hasInternetAccess(): Boolean {
        return try {
            val http = (URL(baseUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = reqMethod
                requestProps?.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }
                connectTimeout = connectionTimeOut.toInt()
            }
            http.connect()

            val responseCode = http.responseCode
            responseCode == 200
        } catch (e: IOException) {
            false
        }
    }

    private companion object{
     private const val DEFAULT_BASE_URL = "https://www.google.com"
        private const val DEFAULT_REQ_METHOD = "GET"
        private const val DEFAULT_CONNECTION_TIMEOUT = 1500L
        private val DEFAULT_REQUEST_PROPS = mapOf(
            "User-Agent" to "Android",
            "Connection" to "Connection",
        )
    }
}
