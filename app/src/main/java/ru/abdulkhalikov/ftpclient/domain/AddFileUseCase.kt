package ru.abdulkhalikov.ftpclient.domain

import org.apache.commons.net.ftp.FTPFile

class AddFileUseCase(private val repository: FTPClientRepository) {

    suspend fun addFile(file: FTPFile) {
        repository.addFile(file)
    }
}