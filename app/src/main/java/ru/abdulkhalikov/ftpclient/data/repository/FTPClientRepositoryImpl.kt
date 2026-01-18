// data/repository/FTPClientRepositoryImpl.kt
package ru.abdulkhalikov.ftpclient.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPClient
import ru.abdulkhalikov.ftpclient.data.mapper.FTPClientMapper
import ru.abdulkhalikov.ftpclient.domain.FTPClientRepository
import ru.abdulkhalikov.ftpclient.domain.RemoteFile
import java.io.InputStream

class FTPClientRepositoryImpl(
    private val ftpClient: FTPClient
) : FTPClientRepository {

    private val mapper = FTPClientMapper()

    override suspend fun getFiles(path: String): List<RemoteFile> {
        return mapper.mapFtpFilesToRemoteFiles(
            list = ftpClient.listFiles(),
            currentPath = getCurrentPath()
        )
    }

    override suspend fun addFile(remotePath: String, local: InputStream): Boolean {
        return true
    }

    override suspend fun removeFile(path: String): Boolean {
        return true
    }

    override suspend fun getCurrentPath(): String {
        return withContext(Dispatchers.IO) {
            try {
                if (ftpClient.isConnected) {
                    ftpClient.printWorkingDirectory() ?: "/"
                } else {
                    "/"
                }
            } catch (e: Exception) {
                "/"
            }
        }
    }
}