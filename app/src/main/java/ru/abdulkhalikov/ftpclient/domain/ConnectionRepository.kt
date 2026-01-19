package ru.abdulkhalikov.ftpclient.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ConnectionRepository {

    val connectionState: Flow<FTPConnectionStatus>

    suspend fun connect(params: ConnectionParams)
}