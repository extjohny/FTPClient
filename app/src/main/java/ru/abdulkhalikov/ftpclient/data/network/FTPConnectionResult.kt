package ru.abdulkhalikov.ftpclient.data.network


sealed interface FTPConnectionResult {

    data object Initial : FTPConnectionResult

    data object Loading : FTPConnectionResult

    data object Success : FTPConnectionResult

    data class Error(val error: String) : FTPConnectionResult
}