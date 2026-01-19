package ru.abdulkhalikov.ftpclient.data.network.state

sealed interface UploadFileResult {

    data object Initial : UploadFileResult

    data object Loading : UploadFileResult

    data class Error(val error: String) : UploadFileResult

    data object Success : UploadFileResult
}