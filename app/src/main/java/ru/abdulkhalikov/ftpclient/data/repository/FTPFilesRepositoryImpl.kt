package ru.abdulkhalikov.ftpclient.data.repository

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.abdulkhalikov.ftpclient.data.mapper.FTPResultMapper.toDomain
import ru.abdulkhalikov.ftpclient.data.network.FTPRemoteDataSource
import ru.abdulkhalikov.ftpclient.domain.FTPFilesRepository

object FTPFilesRepositoryImpl : FTPFilesRepository {

    private val connectionManager = FTPRemoteDataSource

    val files = connectionManager.files.map {
        it.toDomain(currentPath = getCurrentPath())
    }

    val uploadState = connectionManager.uploadState.map {
        it.toDomain()
    }

    override suspend fun getFiles(path: String) {
        connectionManager.getFiles(path)
    }

    override suspend fun addFile(remote: String, local: Uri) {
        connectionManager.uploadFile(local, remote)
        getFiles(remote)
    }

    override suspend fun removeFile(path: String) {

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