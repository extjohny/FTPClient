package ru.abdulkhalikov.ftpclient.domain

import java.io.InputStream

interface FTPClientRepository {

    suspend fun getFiles(path: String = ""): List<RemoteFile>

    suspend fun addFile(remote: String, local: InputStream): Boolean

    suspend fun removeFile(path: String): Boolean

    suspend fun getCurrentPath(): String
}