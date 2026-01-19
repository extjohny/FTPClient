package ru.abdulkhalikov.ftpclient.domain

import android.net.Uri
import javax.inject.Inject

class AddFileUseCase @Inject constructor(
    private val repository: FTPFilesRepository
) {

    suspend fun addFile(remote: String, local: Uri) {
        repository.addFile(remote, local)
    }
}