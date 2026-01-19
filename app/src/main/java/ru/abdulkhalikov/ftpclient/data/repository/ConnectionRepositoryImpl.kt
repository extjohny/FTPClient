package ru.abdulkhalikov.ftpclient.data.repository

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.abdulkhalikov.ftpclient.data.mapper.ConnectionResultMapper.toDomain
import ru.abdulkhalikov.ftpclient.data.network.FTPRemoteDataSource
import ru.abdulkhalikov.ftpclient.domain.ConnectionParams
import ru.abdulkhalikov.ftpclient.domain.ConnectionRepository
import ru.abdulkhalikov.ftpclient.domain.FTPConnectionStatus

class ConnectionRepositoryImpl @Inject constructor(
    private val connectionManager: FTPRemoteDataSource
) : ConnectionRepository {

    override val connectionState: Flow<FTPConnectionStatus> = connectionManager.connectionState
        .map {
            it.toDomain()
        }

    override suspend fun connect(params: ConnectionParams) {
        return connectionManager.connect(params)
    }
}