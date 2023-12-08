package net.sbs.internetchecker.domain

import java.io.IOException

interface IInternetConnectivityChecker {

    @Throws(IOException::class)
    fun hasInternetAccess(): Boolean

    val baseUrl: String
    val reqMethod: String
    val connectionTimeOut: Long
    val requestProps: Map<String, String>?
}