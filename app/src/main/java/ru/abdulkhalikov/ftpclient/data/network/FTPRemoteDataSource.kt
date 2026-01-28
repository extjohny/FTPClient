package ru.abdulkhalikov.ftpclient.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPSClient
import ru.abdulkhalikov.ftpclient.data.network.state.FTPConnectionResult
import ru.abdulkhalikov.ftpclient.data.network.state.GetFTPFilesResult
import ru.abdulkhalikov.ftpclient.data.network.state.RemovingFilesResult
import ru.abdulkhalikov.ftpclient.data.network.state.UploadFileResult
import ru.abdulkhalikov.ftpclient.domain.ConnectionParams
import ru.abdulkhalikov.ftpclient.domain.ProtocolType.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FTPRemoteDataSource @Inject constructor(
    private val context: Context
) {

    var ftpClient = FTPConnectionManager.getFTPClient()

    private val _connectionState =
        MutableStateFlow<FTPConnectionResult>(FTPConnectionResult.Initial)
    val connectionState: StateFlow<FTPConnectionResult> = _connectionState.asStateFlow()

    private val _files =
        MutableStateFlow<GetFTPFilesResult>(GetFTPFilesResult.Initial)
    val files: StateFlow<GetFTPFilesResult> = _files.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadFileResult>(UploadFileResult.Initial)
    val uploadState = _uploadState.asStateFlow()

    private val _removingState = MutableStateFlow<RemovingFilesResult>(RemovingFilesResult.Initial)
    val removingState = _removingState.asStateFlow()

    suspend fun connect(
        params: ConnectionParams
    ) {
        withContext(Dispatchers.IO) {
            try {
                ftpClient = FTPConnectionManager.getFTPClient(params.protocolType)

                if (ftpClient.isConnected) {
                    try {
                        ftpClient.disconnect()
                    } catch (e: Exception) {
                    }
                }

                _connectionState.value = FTPConnectionResult.Loading

                ftpClient.connect(params.host, params.port)

                if (!ftpClient.isConnected) {
                    _connectionState.value = FTPConnectionResult.Error("Connection failed")
                    return@withContext
                }

                val loginSuccess = ftpClient.login(params.username, params.password)
                if (!loginSuccess) {
                    _connectionState.value = FTPConnectionResult.Error("Incorrect input data")
                    return@withContext
                }
                if (ftpClient is FTPSClient) {
                    try {
                        (ftpClient as FTPSClient).execPROT("P")
                        ftpClient.enterLocalPassiveMode()
                    } catch (e: Exception) {
                        _connectionState.value =
                            FTPConnectionResult.Error("FTPS SSL setup failed: ${e.message}")
                        return@withContext
                    }
                }

                _connectionState.value = FTPConnectionResult.Success

                return@withContext
            } catch (e: IOException) {
                _connectionState.value = FTPConnectionResult.Error(e.message ?: "IO Error")
            } catch (e: Exception) {
                _connectionState.value = FTPConnectionResult.Error(e.message ?: "Unknown error")
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

    suspend fun removeFile(remotePath: String) {
        withContext(Dispatchers.IO) {
            _removingState.value = RemovingFilesResult.Loading
            try {
                ftpClient.deleteFile(remotePath)
            } catch (e: IOException) {
                _uploadState.value = UploadFileResult.Error(e.message ?: "IO Error")
            } catch (e: Exception) {
                _uploadState.value = UploadFileResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun uploadFile(localUri: Uri, remotePath: String) {
        withContext(Dispatchers.IO) {
            _uploadState.value = UploadFileResult.Loading
            try {
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

    suspend fun createDirectory(path: String, directoryName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val fullPath = if (path.endsWith("/")) {
                    "$path$directoryName"
                } else {
                    "$path/$directoryName"
                }
                val success = ftpClient.makeDirectory(fullPath)
                if (!success) {
                    Log.e("LOG_TAG", "Failed to create directory: ${ftpClient.replyString}")
                }
                success
            } catch (e: Exception) {
                Log.e("LOG_TAG", "Error creating directory", e)
                false
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex =
                        cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
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

    suspend fun downloadFileToTemp(remotePath: String, localFile: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Создаем поток для записи
                FileOutputStream(localFile).use { outputStream ->
                    // Скачиваем файл с сервера
                    val success = ftpClient.retrieveFile(remotePath, outputStream)

                    if (!success) {
                        Log.e("FTPRemoteDataSource", "Ошибка скачивания файла: ${ftpClient.replyString}")
                    }

                    success
                }
            } catch (e: Exception) {
                Log.e("FTPRemoteDataSource", "Ошибка скачивания файла: ${e.message}")
                false
            }
        }
    }
}