package ru.abdulkhalikov.ftpclient.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPClient
import ru.abdulkhalikov.ftpclient.domain.ConnectionParams
import java.io.IOException

object FTPRemoteDataSource {

    val ftpClient = FTPClient()

    private val _connectionState =
        MutableStateFlow<FTPConnectionResult>(FTPConnectionResult.Initial)
    val connectionState: StateFlow<FTPConnectionResult> = _connectionState.asStateFlow()

    suspend fun connect(
        params: ConnectionParams
    ) {
        withContext(Dispatchers.IO) {
            try {
                ftpClient.connect(params.host, params.port)

                _connectionState.value = FTPConnectionResult.Loading

                if (!ftpClient.isConnected) {
                    _connectionState.value = FTPConnectionResult.Error("Connection failed")
                    return@withContext
                }

                val loginSuccess = ftpClient.login(params.username, params.password)
                if (!loginSuccess) {
                    _connectionState.value = FTPConnectionResult.Error("Incorrect input data")
                    return@withContext
                }

                ftpClient.enterLocalPassiveMode()

                _connectionState.value = FTPConnectionResult.Success

                return@withContext
            } catch (e: IOException) {
                _connectionState.value = FTPConnectionResult.Error(e.message.toString())
            } catch (e: Exception) {
                _connectionState.value = FTPConnectionResult.Error(e.message.toString())
            }
        }
    }
}