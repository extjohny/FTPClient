package ru.abdulkhalikov.ftpclient.presentation.ui.connection

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.abdulkhalikov.ftpclient.data.repository.ConnectionRepositoryImpl
import ru.abdulkhalikov.ftpclient.domain.ConnectUseCase
import ru.abdulkhalikov.ftpclient.domain.ConnectionParams
import ru.abdulkhalikov.ftpclient.domain.FTPConnectionStatus

@Stable
class ConnectionViewModel() : ViewModel() {

    private val repository = ConnectionRepositoryImpl

    private val connectUseCase = ConnectUseCase(repository)

    val screenState: StateFlow<FTPConnectionStatus> = repository.connectionState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(),
            initialValue = FTPConnectionStatus.Initial
        )

    fun connect(
        host: String,
        port: Int = 21,
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            val connectionParams = ConnectionParams(host, port, username, password)
            connectUseCase.connect(connectionParams)
        }
    }
}