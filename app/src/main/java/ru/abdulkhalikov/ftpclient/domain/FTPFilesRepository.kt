package ru.abdulkhalikov.ftpclient.domain

import java.io.InputStream

interface FTPFilesRepository {

    suspend fun getFiles(path: String = "")

    suspend fun addFile(remote: String, local: InputStream): Boolean

    suspend fun removeFile(path: String): Boolean

    suspend fun getCurrentPath(): String
}