package ru.abdulkhalikov.ftpclient.data.mapper

import ru.abdulkhalikov.ftpclient.data.network.manage_state.FTPConnectionResult
import ru.abdulkhalikov.ftpclient.domain.FTPConnectionStatus

object ConnectionResultMapper {

    fun FTPConnectionResult.toDomain(): FTPConnectionStatus {
        return when (this) {
            is FTPConnectionResult.Error -> FTPConnectionStatus.Error(this.error)
            FTPConnectionResult.Loading -> FTPConnectionStatus.Loading
            FTPConnectionResult.Success -> FTPConnectionStatus.Success
            FTPConnectionResult.Initial -> FTPConnectionStatus.Initial
        }
    }
}