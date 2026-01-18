package ru.abdulkhalikov.ftpclient.presentation.ui.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.abdulkhalikov.ftpclient.data.repository.FTPFilesRepositoryImpl
import ru.abdulkhalikov.ftpclient.domain.GetFTPFilesStatus
import ru.abdulkhalikov.ftpclient.domain.GetFilesUseCase

class FilesViewModel() : ViewModel() {

    private val repository = FTPFilesRepositoryImpl

    private val getFilesUseCase = GetFilesUseCase(repository)

    val screenState: StateFlow<GetFTPFilesStatus> = repository.files
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = GetFTPFilesStatus.Initial
        )

    init {
        getFiles()
    }

    private fun getFiles() {
        viewModelScope.launch {
            getFilesUseCase.getFiles()
        }
    }
}