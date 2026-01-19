package ru.abdulkhalikov.ftpclient.domain

import javax.inject.Inject

class CreateDirectoryUseCase @Inject constructor(
    private val repository: FTPFilesRepository
) {

    suspend fun createDirectory(path: String, directoryName: String): Boolean {
        return repository.createDirectory(path, directoryName)
    }
}
