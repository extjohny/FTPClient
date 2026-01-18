package ru.abdulkhalikov.ftpclient.domain

import java.io.InputStream

class AddFileUseCase(private val repository: FTPFilesRepository) {

    suspend fun addFile(remote: String, local: InputStream) {
        repository.addFile(remote, local)
    }
}