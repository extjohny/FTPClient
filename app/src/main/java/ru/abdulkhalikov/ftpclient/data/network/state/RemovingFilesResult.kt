package ru.abdulkhalikov.ftpclient.data.network.state

sealed interface RemovingFilesResult {

    data object Initial : RemovingFilesResult

    data object Loading : RemovingFilesResult

    data class Error(val error: String) : RemovingFilesResult

    data object Success : RemovingFilesResult
}