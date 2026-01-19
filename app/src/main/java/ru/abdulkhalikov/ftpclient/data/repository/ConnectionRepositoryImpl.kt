package ru.abdulkhalikov.ftpclient.data.repository

import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.abdulkhalikov.ftpclient.data.mapper.ConnectionResultMapper.toDomain
import ru.abdulkhalikov.ftpclient.data.network.FTPRemoteDataSource
import ru.abdulkhalikov.ftpclient.domain.ConnectionParams
import ru.abdulkhalikov.ftpclient.domain.ConnectionRepository
import ru.abdulkhalikov.ftpclient.domain.FTPConnectionStatus

class ConnectionRepositoryImpl @Inject constructor(
    private val connectionManager: FTPRemoteDataSource
) : ConnectionRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob())

    override val connectionState: StateFlow<FTPConnectionStatus> = connectionManager.connectionState
        .map {
            it.toDomain()
        }
        .stateIn(
            scope = repositoryScope,
            started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
            initialValue = FTPConnectionStatus.Initial
        )

    override suspend fun connect(params: ConnectionParams) {
        return connectionManager.connect(params)
    }
}