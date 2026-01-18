package ru.abdulkhalikov.ftpclient.domain

sealed interface GetFTPFilesStatus {

    data object Initial : GetFTPFilesStatus

    data object Loading : GetFTPFilesStatus

    data class Error(val error: String) : GetFTPFilesStatus

    data class Success(val files: List<RemoteFile>) : GetFTPFilesStatus
}