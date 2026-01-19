package ru.abdulkhalikov.ftpclient.presentation.ui.files

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.abdulkhalikov.ftpclient.domain.AddFileUseCase
import ru.abdulkhalikov.ftpclient.domain.CreateDirectoryUseCase
import ru.abdulkhalikov.ftpclient.domain.FTPFilesRepository
import ru.abdulkhalikov.ftpclient.domain.GetFTPFilesStatus
import ru.abdulkhalikov.ftpclient.domain.GetFilesUseCase
import ru.abdulkhalikov.ftpclient.domain.RemoteFile
import ru.abdulkhalikov.ftpclient.domain.UploadFilesStatus
import javax.inject.Inject

class FilesViewModel @Inject constructor(
    private val repository: FTPFilesRepository,
    private val getFilesUseCase: GetFilesUseCase,
    private val addFileUseCase: AddFileUseCase,
    private val createDirectoryUseCase: CreateDirectoryUseCase
) : ViewModel() {

    val screenState: StateFlow<GetFTPFilesStatus> = repository.files
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = GetFTPFilesStatus.Initial
        )

    val uploadState = repository.uploadState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = UploadFilesStatus.Initial
        )

    private val _remoteCurrentPath = MutableStateFlow("/")
    val remoteCurrentPath: StateFlow<String> = _remoteCurrentPath.asStateFlow()

    init {
        getFiles(_remoteCurrentPath.value)
    }

    private fun getFiles(path: String) {
        viewModelScope.launch {
            getFilesUseCase.getFiles(path)
        }
    }

    fun addFile(localFileUri: Uri) {
        viewModelScope.launch {
            addFileUseCase.addFile(_remoteCurrentPath.value, localFileUri)
        }
    }

    fun navigateToDirectory(remoteFile: RemoteFile) {
        if (remoteFile.isDirectory) {
            val currentPath = _remoteCurrentPath.value
            val newPath = if (currentPath.endsWith("/")) {
                currentPath + remoteFile.name
            } else {
                "$currentPath/${remoteFile.name}"
            }
            _remoteCurrentPath.value = newPath
            getFiles(newPath)
        }
    }

    fun navigateBack() {
        val currentPath = _remoteCurrentPath.value.trimEnd('/')
        if (currentPath == "" || currentPath == "/") {
            return
        }
        val parentPath = currentPath.substringBeforeLast('/', "/")
        _remoteCurrentPath.value = parentPath
        getFiles(parentPath)
    }

    fun canNavigateBack(): Boolean {
        val currentPath = _remoteCurrentPath.value.trimEnd('/')
        return currentPath != "" && currentPath != "/"
    }

    private val _createDirectoryResult = MutableStateFlow<String?>(null)
    val createDirectoryResult: StateFlow<String?> = _createDirectoryResult.asStateFlow()

    fun createDirectory(directoryName: String) {
        viewModelScope.launch {
            _createDirectoryResult.value = null
            val success = createDirectoryUseCase.createDirectory(_remoteCurrentPath.value, directoryName)
            if (success) {
                _createDirectoryResult.value = "Directory created successfully"
                // Обновляем список файлов после создания папки
                getFiles(_remoteCurrentPath.value)
            } else {
                _createDirectoryResult.value = "Failed to create directory"
            }
        }
    }

    fun clearCreateDirectoryResult() {
        _createDirectoryResult.value = null
    }
}