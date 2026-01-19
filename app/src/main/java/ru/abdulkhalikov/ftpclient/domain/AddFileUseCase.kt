package ru.abdulkhalikov.ftpclient.domain

import android.net.Uri

class AddFileUseCase(private val repository: FTPFilesRepository) {

    suspend fun addFile(remote: String, local: Uri) {
        repository.addFile(remote, local)
    }
}