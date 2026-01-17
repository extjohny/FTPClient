package ru.abdulkhalikov.ftpclient.domain

import kotlinx.coroutines.flow.Flow
import org.apache.commons.net.ftp.FTPFile

class GetFilesUseCase(private val repository: FTPClientRepository) {

    suspend fun getFiles(): Flow<List<FTPFile>> {
        return repository.getFiles()
    }
}