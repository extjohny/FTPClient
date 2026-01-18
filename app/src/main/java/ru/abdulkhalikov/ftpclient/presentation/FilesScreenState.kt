package ru.abdulkhalikov.ftpclient.presentation

import ru.abdulkhalikov.ftpclient.domain.RemoteFile

sealed interface FilesScreenState {

    data object Initial : FilesScreenState

    data object Loading : FilesScreenState

    data class Success(val files: List<RemoteFile>) : FilesScreenState

    data class Error(val error: String) : FilesScreenState
}