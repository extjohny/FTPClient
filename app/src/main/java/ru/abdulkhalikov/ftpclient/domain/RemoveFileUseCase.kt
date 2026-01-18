package ru.abdulkhalikov.ftpclient.domain

class RemoveFileUseCase(private val repository: FTPFilesRepository) {

    suspend fun removeFile(path: String) {
        repository.removeFile(path)
    }
}