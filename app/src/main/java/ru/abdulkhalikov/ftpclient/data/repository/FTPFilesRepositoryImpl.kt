package ru.abdulkhalikov.ftpclient.data.repository

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.abdulkhalikov.ftpclient.data.mapper.FTPResultMapper.toDomain
import ru.abdulkhalikov.ftpclient.data.network.FTPRemoteDataSource
import ru.abdulkhalikov.ftpclient.domain.FTPFilesRepository
import javax.inject.Inject

class FTPFilesRepositoryImpl @Inject constructor(
    private val connectionManager: FTPRemoteDataSource
) : FTPFilesRepository {

    override val files = connectionManager.files
        .map {
            it.toDomain(currentPath = getCurrentPath())
        }

    override val uploadState = connectionManager.uploadState
        .map {
            it.toDomain()
        }

    override suspend fun getFiles(path: String) {
        connectionManager.getFiles(path)
    }

    override suspend fun addFile(remote: String, local: Uri) {
        connectionManager.uploadFile(local, remote)
        val currentPath = getCurrentPath()
        getFiles(currentPath)
    }

    override suspend fun removeFile(path: String) {
        connectionManager.removeFile(path)
    }

    override suspend fun createDirectory(path: String, directoryName: String): Boolean {
        return connectionManager.createDirectory(path, directoryName)
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