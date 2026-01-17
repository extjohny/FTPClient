package ru.abdulkhalikov.ftpclient.domain

import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface FTPClientRepository {

    val files: Flow<List<RemoteFile>>

    suspend fun addFile(remote: String, local: InputStream)

    suspend fun removeFile(path: String)
}