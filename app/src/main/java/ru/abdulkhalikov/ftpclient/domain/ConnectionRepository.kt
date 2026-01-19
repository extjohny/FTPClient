package ru.abdulkhalikov.ftpclient.domain

import kotlinx.coroutines.flow.StateFlow

interface ConnectionRepository {

    val connectionState: StateFlow<FTPConnectionStatus>

    suspend fun connect(params: ConnectionParams)
}