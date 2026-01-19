package ru.abdulkhalikov.ftpclient.domain

import android.net.Uri

interface FTPFilesRepository {

    suspend fun getFiles(path: String = "")

    suspend fun addFile(remote: String, local: Uri)

    suspend fun removeFile(path: String)

    suspend fun getCurrentPath(): String
}