package ru.abdulkhalikov.ftpclient.presentation.ui.connection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
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

    fun connect(
        host: String,
        port: Int = 21,
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            Log.d("LOG_TAG", "connection state: try to connect")
            val connectionParams = ConnectionParams(host, port, username, password)
            connectUseCase.connect(connectionParams)
        }
    }
}