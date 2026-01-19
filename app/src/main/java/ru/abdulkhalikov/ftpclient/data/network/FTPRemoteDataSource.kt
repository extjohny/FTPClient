package ru.abdulkhalikov.ftpclient.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.abdulkhalikov.ftpclient.data.network.state.FTPConnectionResult
import ru.abdulkhalikov.ftpclient.data.network.state.GetFTPFilesResult
import ru.abdulkhalikov.ftpclient.data.network.state.UploadFileResult
import ru.abdulkhalikov.ftpclient.domain.ConnectionParams
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FTPRemoteDataSource @Inject constructor(
    private val context: Context
) {

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
            _uploadState.value = UploadFileResult.Loading
            try {
                // Получаем имя файла из URI
                val fileName = getFileNameFromUri(localUri) ?: "uploaded_file"
                val fullRemotePath = if (remotePath.endsWith("/")) {
                    remotePath + fileName
                } else {
                    "$remotePath/$fileName"
                }

                context.contentResolver.openInputStream(localUri).use { inputStream ->
                    val success = ftpClient.storeFile(fullRemotePath, inputStream)
                    if (success) {
                        _uploadState.value = UploadFileResult.Success
                        Log.d("LOG_TAG", "Upload file: success state set")
                    } else {
                        val replyString = ftpClient.replyString
                        _uploadState.value = UploadFileResult.Error("Upload failed: $replyString")
                    }
                }
            } catch (e: IOException) {
                _uploadState.value = UploadFileResult.Error(e.message ?: "IO Error")
            } catch (e: Exception) {
                _uploadState.value = UploadFileResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.let {
                val cut = it.lastIndexOf('/')
                if (cut != -1) {
                    it.substring(cut + 1)
                } else {
                    null
                }
            }
        }
        return result
    }
}