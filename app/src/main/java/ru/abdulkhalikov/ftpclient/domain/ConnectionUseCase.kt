package ru.abdulkhalikov.ftpclient.domain

class ConnectionUseCase(private val repository: FTPClientRepository) {

    suspend fun connect(
        host: String,
        port: Int = 21,
        username: String,
        password: String
    ) {
        repository.connect(host, port, username, password)
    }
}