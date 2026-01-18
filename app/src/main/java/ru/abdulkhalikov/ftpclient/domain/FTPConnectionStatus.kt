package ru.abdulkhalikov.ftpclient.domain

sealed interface FTPConnectionStatus {

    data object Initial : FTPConnectionStatus

    data object Loading : FTPConnectionStatus

    data object Success : FTPConnectionStatus

    data class Error(val error: String) : FTPConnectionStatus
}