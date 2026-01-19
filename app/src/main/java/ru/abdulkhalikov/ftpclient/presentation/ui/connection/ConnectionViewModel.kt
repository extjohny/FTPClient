package ru.abdulkhalikov.ftpclient.presentation.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.abdulkhalikov.ftpclient.domain.ConnectUseCase
import ru.abdulkhalikov.ftpclient.domain.ConnectionParams
import ru.abdulkhalikov.ftpclient.domain.ConnectionRepository
import ru.abdulkhalikov.ftpclient.domain.FTPConnectionStatus
import javax.inject.Inject

class ConnectionViewModel @Inject constructor(
    private val repository: ConnectionRepository,
    private val connectUseCase: ConnectUseCase
) : ViewModel() {

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