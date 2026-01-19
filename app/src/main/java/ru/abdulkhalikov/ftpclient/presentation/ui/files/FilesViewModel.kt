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
import ru.abdulkhalikov.ftpclient.domain.FTPFilesRepository
import ru.abdulkhalikov.ftpclient.domain.GetFTPFilesStatus
import ru.abdulkhalikov.ftpclient.domain.GetFilesUseCase
import ru.abdulkhalikov.ftpclient.domain.RemoteFile
import ru.abdulkhalikov.ftpclient.domain.UploadFilesStatus
import javax.inject.Inject

class FilesViewModel @Inject constructor(
    private val repository: FTPFilesRepository,
    private val getFilesUseCase: GetFilesUseCase,
    private val addFileUseCase: AddFileUseCase
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
            _remoteCurrentPath.value += remoteFile.name
            getFiles(_remoteCurrentPath.value)
        }
    }
}