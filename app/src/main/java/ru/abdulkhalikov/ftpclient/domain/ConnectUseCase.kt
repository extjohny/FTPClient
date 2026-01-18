package ru.abdulkhalikov.ftpclient.domain

class ConnectUseCase(private val repository: ConnectionRepository) {

    suspend fun connect(params: ConnectionParams) {
        repository.connect(params)
    }
}