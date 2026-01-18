package ru.abdulkhalikov.ftpclient.domain

class GetFilesUseCase(private val repository: FTPClientRepository) {

    suspend fun getFiles(): List<RemoteFile> {
        return repository.getFiles()
    }
}