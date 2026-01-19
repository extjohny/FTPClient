package ru.abdulkhalikov.ftpclient.domain

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface FTPFilesRepository {

    val files: Flow<GetFTPFilesStatus>

    val uploadState: Flow<UploadFilesStatus>

    suspend fun getFiles(path: String = "")

    suspend fun addFile(remote: String, local: Uri)

    suspend fun removeFile(path: String)

    suspend fun createDirectory(path: String, directoryName: String): Boolean

    suspend fun getCurrentPath(): String
}