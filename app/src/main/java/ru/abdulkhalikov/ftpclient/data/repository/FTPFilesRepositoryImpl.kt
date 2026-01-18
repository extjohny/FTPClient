package ru.abdulkhalikov.ftpclient.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.abdulkhalikov.ftpclient.data.mapper.FTPClientMapper.toDomain
import ru.abdulkhalikov.ftpclient.data.network.FTPRemoteDataSource
import ru.abdulkhalikov.ftpclient.domain.FTPFilesRepository
import java.io.InputStream

object FTPFilesRepositoryImpl : FTPFilesRepository {

    private val connectionManager = FTPRemoteDataSource

    val files = connectionManager.files.map {
        it.toDomain(currentPath = getCurrentPath())
    }

    override suspend fun getFiles(path: String) {
        connectionManager.getFiles()
    }

    override suspend fun addFile(remote: String, local: InputStream): Boolean {
        return true
    }

    override suspend fun removeFile(path: String): Boolean {
        return true
    }

    override suspend fun getCurrentPath(): String {
        return withContext(Dispatchers.IO) {
            try {
                val ftpClient = connectionManager.ftpClient
                if (ftpClient.isConnected) {
                    ftpClient.printWorkingDirectory() ?: "/"
                } else {
                    "/"
                }
            } catch (_: Exception) {
                "/"
            }
        }
    }
}