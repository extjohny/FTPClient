package ru.abdulkhalikov.ftpclient.data.repository

import android.util.Log
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map
import ru.abdulkhalikov.ftpclient.data.mapper.ConnectionResultMapper.toDomain
import ru.abdulkhalikov.ftpclient.data.network.FTPRemoteDataSource
import ru.abdulkhalikov.ftpclient.domain.ConnectionParams
import ru.abdulkhalikov.ftpclient.domain.ConnectionRepository

class ConnectionRepositoryImpl @Inject constructor(
    private val connectionManager: FTPRemoteDataSource
) : ConnectionRepository {

    override val connectionState = connectionManager.connectionState
        .map {
            Log.d("LOG_TAG", "connection state: map data to domain state: $it")
            it.toDomain()
        }

    override suspend fun connect(params: ConnectionParams) {
        return connectionManager.connect(params)
    }
}