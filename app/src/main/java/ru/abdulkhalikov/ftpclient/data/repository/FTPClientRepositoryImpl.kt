package ru.abdulkhalikov.ftpclient.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPFile
import ru.abdulkhalikov.ftpclient.data.mapper.FTPClientMapper
import ru.abdulkhalikov.ftpclient.data.network.FTPClient
import ru.abdulkhalikov.ftpclient.domain.FTPClientRepository
import ru.abdulkhalikov.ftpclient.domain.RemoteFile
import java.io.InputStream

class FTPClientRepositoryImpl : FTPClientRepository {

    private val ftpClient = FTPClient.create()

    private val mapper = FTPClientMapper()

    override val files: Flow<List<RemoteFile>> = flow {
        emit(mapper.mapFtpFilesToRemoteFiles(ftpClient.listFiles()))
    }

    override suspend fun addFile(remote: String, local: InputStream) {
        withContext(Dispatchers.IO) {
            ftpClient.storeFile(remote, local)
        }
    }

    override suspend fun removeFile(path: String) {
        ftpClient.deleteFile(path)
    }
}