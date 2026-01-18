package ru.abdulkhalikov.ftpclient.domain

class RemoveFileUseCase(private val repository: FTPClientRepository) {

    suspend fun removeFile(path: String) {
        repository.removeFile(path)
    }
}