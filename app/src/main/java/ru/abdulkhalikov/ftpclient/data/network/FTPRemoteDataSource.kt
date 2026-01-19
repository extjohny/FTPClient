package ru.abdulkhalikov.ftpclient.data.network

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.abdulkhalikov.ftpclient.MyApp
import ru.abdulkhalikov.ftpclient.data.network.manage_state.FTPConnectionResult
import ru.abdulkhalikov.ftpclient.data.network.manage_state.GetFTPFilesResult
import ru.abdulkhalikov.ftpclient.data.network.manage_state.UploadFileResult
import ru.abdulkhalikov.ftpclient.domain.ConnectionParams
import java.io.IOException

object FTPRemoteDataSource {

    private val context = MyApp()

    val ftpClient = FTPConnectionManager.getFTPClient()

    private val _connectionState =
        MutableStateFlow<FTPConnectionResult>(FTPConnectionResult.Initial)
    val connectionState: StateFlow<FTPConnectionResult> = _connectionState.asStateFlow()

    private val _files =
        MutableStateFlow<GetFTPFilesResult>(GetFTPFilesResult.Initial)
    val files: StateFlow<GetFTPFilesResult> = _files.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadFileResult>(UploadFileResult.Initial)
    val uploadState = _uploadState.asStateFlow()

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

    suspend fun getFiles(path: String = "/") {
        withContext(Dispatchers.IO) {
            try {
                _files.value = GetFTPFilesResult.Loading
                val files = ftpClient.listFiles(path)
                _files.value = GetFTPFilesResult.Success(files)
            } catch (e: IOException) {
                _files.value = GetFTPFilesResult.Error(e.message.toString())
            } catch (e: Exception) {
                _files.value = GetFTPFilesResult.Error(e.message.toString())
            }
        }
    }

    suspend fun uploadFile(localUri: Uri, remotePath: String) {
        withContext(Dispatchers.IO) {
            return@withContext try {
                context.contentResolver.openInputStream(localUri).use { inputStream ->
                    val success = ftpClient.storeFile(remotePath, inputStream)
                    if (success) {
                        _uploadState.value = UploadFileResult.Success
                    } else {
                        _uploadState.value = UploadFileResult.Error("Upload failed")
                    }
                }
            } catch (e: IOException) {
                _uploadState.value = UploadFileResult.Error(e.message.toString())
            }
        }
    }
}