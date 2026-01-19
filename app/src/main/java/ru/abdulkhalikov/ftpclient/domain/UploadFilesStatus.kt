package ru.abdulkhalikov.ftpclient.domain

sealed interface UploadFilesStatus {

    data object Initial : UploadFilesStatus

    data object Loading : UploadFilesStatus

    data class Error(val error: String) : UploadFilesStatus

    data object Success : UploadFilesStatus
}