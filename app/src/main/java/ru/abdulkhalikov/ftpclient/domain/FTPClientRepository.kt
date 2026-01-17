package ru.abdulkhalikov.ftpclient.domain

import kotlinx.coroutines.flow.Flow
import org.apache.commons.net.ftp.FTPFile

interface FTPClientRepository {

    val files: Flow<List<FTPFile>>

    suspend fun addFile(file: FTPFile)

    suspend fun removeFile(file: FTPFile)
}