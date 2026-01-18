package ru.abdulkhalikov.ftpclient.domain

import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream

class AddFileUseCase(private val repository: FTPClientRepository) {

    suspend fun addFile(remote: String, local: InputStream) {
        repository.addFile(remote, local)
    }
}