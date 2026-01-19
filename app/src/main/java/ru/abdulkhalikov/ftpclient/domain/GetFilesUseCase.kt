package ru.abdulkhalikov.ftpclient.domain

import javax.inject.Inject

class GetFilesUseCase @Inject constructor(
    private val repository: FTPFilesRepository
) {

    suspend fun getFiles(remotePath: String) {
        return repository.getFiles(remotePath)
    }
}