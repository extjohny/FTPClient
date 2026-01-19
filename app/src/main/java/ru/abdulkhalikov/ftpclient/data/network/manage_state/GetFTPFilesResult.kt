package ru.abdulkhalikov.ftpclient.data.network.manage_state

import org.apache.commons.net.ftp.FTPFile

sealed interface GetFTPFilesResult {

    data object Initial : GetFTPFilesResult

    data object Loading : GetFTPFilesResult

    data class Error(val error: String) : GetFTPFilesResult

    data class Success(val files: Array<FTPFile?>) : GetFTPFilesResult
}