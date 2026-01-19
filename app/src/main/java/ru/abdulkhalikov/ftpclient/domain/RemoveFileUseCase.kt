package ru.abdulkhalikov.ftpclient.domain

import javax.inject.Inject

class RemoveFileUseCase @Inject constructor(
    private val repository: FTPFilesRepository
) {

    suspend fun removeFile(path: String) {
        repository.removeFile(path)
    }
}