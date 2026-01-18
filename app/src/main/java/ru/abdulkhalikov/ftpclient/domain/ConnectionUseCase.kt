package ru.abdulkhalikov.ftpclient.domain

class ConnectionUseCase(private val repository: FTPClientRepository) {

    suspend fun connect() {
        repository.connect()
    }
}