package ru.abdulkhalikov.ftpclient.domain

import org.apache.commons.net.ftp.FTPFile

class RemoveFileUseCase(private val repository: FTPClientRepository) {

    suspend fun removeFile(file: FTPFile) {
        repository.removeFile(file)
    }
}