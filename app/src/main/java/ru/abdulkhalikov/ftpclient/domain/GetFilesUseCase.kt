package ru.abdulkhalikov.ftpclient.domain

class GetFilesUseCase(private val repository: FTPFilesRepository) {

    suspend fun getFiles(remotePath: String) {
        return repository.getFiles(remotePath)
    }
}