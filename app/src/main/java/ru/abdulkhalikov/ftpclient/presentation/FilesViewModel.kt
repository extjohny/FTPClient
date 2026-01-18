package ru.abdulkhalikov.ftpclient.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.abdulkhalikov.ftpclient.data.network.FTPConnectionManager
import ru.abdulkhalikov.ftpclient.data.repository.FTPClientRepositoryImpl

class FilesViewModel() : ViewModel() {

    private lateinit var repository: FTPClientRepositoryImpl

    private val _screenState = MutableStateFlow<FilesScreenState>(FilesScreenState.Initial)
    val screenState: StateFlow<FilesScreenState> = _screenState.asStateFlow()

    init {
        getFiles()
    }

    private fun getFiles() = viewModelScope.launch {
        repository = FTPClientRepositoryImpl(FTPConnectionManager.get())
        _screenState.value = FilesScreenState.Loading
        _screenState.value = FilesScreenState.Success(
            files = repository.getFiles()
        )
    }
}