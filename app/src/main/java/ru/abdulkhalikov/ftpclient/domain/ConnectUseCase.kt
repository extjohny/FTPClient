package ru.abdulkhalikov.ftpclient.domain

import javax.inject.Inject

class ConnectUseCase @Inject constructor(
    private val repository: ConnectionRepository
) {

    suspend fun connect(params: ConnectionParams) {
        repository.connect(params)
    }
}