package net.sbs.internetchecker.domain

import kotlinx.coroutines.flow.Flow

interface IConnectivityObserver {
    fun observe(): Flow<Status>

    enum class Status {
        Available, AvailableHardware, Unavailable, UnavailableHardware, Losing, Lost
    }
}