package ru.abdulkhalikov.ftpclient.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPClient
import ru.abdulkhalikov.ftpclient.domain.ConnectionParams
import java.io.IOException

object FTPConnectionManager {

    val ftpClient = FTPClient()

    private val _connectionState = MutableStateFlow<FTPConnectionState>(FTPConnectionState.Initial)
    val connectionState: StateFlow<FTPConnectionState> = _connectionState.asStateFlow()

    suspend fun connect(
        params: ConnectionParams
    ) {
        withContext(Dispatchers.IO) {
            try {
                ftpClient.connect(params.host, params.port)

                _connectionState.value = FTPConnectionState.Loading

                if (!ftpClient.isConnected) {
                    _connectionState.value = FTPConnectionState.Error("Connection failed")
                    return@withContext
                }

                val loginSuccess = ftpClient.login(params.username, params.password)
                if (!loginSuccess) {
                    _connectionState.value = FTPConnectionState.Error("Incorrect input data")
                    return@withContext
                }

                ftpClient.enterLocalPassiveMode()

                _connectionState.value = FTPConnectionState.Success

                return@withContext
            } catch (e: IOException) {
                _connectionState.value = FTPConnectionState.Error(e.message.toString())
            } catch (e: Exception) {
                _connectionState.value = FTPConnectionState.Error(e.message.toString())
            }
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        ftpClient.logout()
        _connectionState.value = FTPConnectionState.Disconnected
    }
}