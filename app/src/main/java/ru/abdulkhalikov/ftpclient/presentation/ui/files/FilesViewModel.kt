package ru.abdulkhalikov.ftpclient.presentation.ui.files

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.abdulkhalikov.ftpclient.ai.FileClassifier
import ru.abdulkhalikov.ftpclient.domain.AddFileUseCase
import ru.abdulkhalikov.ftpclient.domain.CreateDirectoryUseCase
import ru.abdulkhalikov.ftpclient.domain.FTPFilesRepository
import ru.abdulkhalikov.ftpclient.domain.GetFTPFilesStatus
import ru.abdulkhalikov.ftpclient.domain.GetFilesUseCase
import ru.abdulkhalikov.ftpclient.domain.RemoteFile
import ru.abdulkhalikov.ftpclient.domain.RemoveFileUseCase
import ru.abdulkhalikov.ftpclient.domain.UploadFilesStatus
import javax.inject.Inject

class FilesViewModel @Inject constructor(
    private val repository: FTPFilesRepository,
    private val getFilesUseCase: GetFilesUseCase,
    private val addFileUseCase: AddFileUseCase,
    private val createDirectoryUseCase: CreateDirectoryUseCase,
    private val removeFileUseCase: RemoveFileUseCase,
    private val context: Context // Добавляем Context
) : ViewModel() {

    private val fileClassifier = FileClassifier(context)
    private val classificationCache = mutableMapOf<String, FileClassifier.ClassificationResult>()

    // Состояние для UI
    private val _classificationState = MutableStateFlow<ClassificationState>(
        ClassificationState.Idle
    )
    val classificationState: StateFlow<ClassificationState> = _classificationState

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
        if (currentPath.isEmpty() || currentPath == "/") {
            return
        }

        val lastSlashIndex = currentPath.lastIndexOf('/')
        val parentPath = if (lastSlashIndex > 0) {
            currentPath.substring(0, lastSlashIndex)
        } else {
            "/"
        }

        _remoteCurrentPath.value = parentPath
        getFiles(parentPath)
    }

    fun canNavigateBack(): Boolean {
        val currentPath = _remoteCurrentPath.value.trimEnd('/')
        return currentPath.isNotEmpty() && currentPath != "/"
    }

    private val _createDirectoryResult = MutableStateFlow<String?>(null)
    val createDirectoryResult: StateFlow<String?> = _createDirectoryResult.asStateFlow()

    fun createDirectory(directoryName: String) {
        viewModelScope.launch {
            _createDirectoryResult.value = null
            val success =
                createDirectoryUseCase.createDirectory(_remoteCurrentPath.value, directoryName)
            if (success) {
                _createDirectoryResult.value = "Directory created successfully"
                getFiles(_remoteCurrentPath.value)
            } else {
                _createDirectoryResult.value = "Failed to create directory"
            }
        }
    }

    fun clearCreateDirectoryResult() {
        _createDirectoryResult.value = null
    }

    fun removeFile(remoteFile: RemoteFile) {
        viewModelScope.launch {
            removeFileUseCase.removeFile(remoteFile.path)
            getFiles(_remoteCurrentPath.value)
        }
    }

    /**
     * Классифицировать файл с помощью ИИ
     */
    fun classifyFile(remoteFile: RemoteFile) {
        // Не классифицируем папки и большие файлы (> 50MB)
        if (remoteFile.isDirectory || remoteFile.size > 50 * 1024 * 1024) {
            _classificationState.value = ClassificationState.Error(
                file = remoteFile,
                message = "Файл слишком большой или это папка"
            )
            return
        }

        // Проверяем кэш
        classificationCache[remoteFile.path]?.let { result ->
            _classificationState.value = ClassificationState.Result(remoteFile, result)
            return
        }

        viewModelScope.launch {
            try {
                _classificationState.value = ClassificationState.Loading(remoteFile)

                // Для тестирования используем заглушку (в реальном проекте скачиваем файл)
                // Вместо реального скачивания используем заглушку для демонстрации
                val result = simulateClassification(remoteFile)

                // Сохраняем в кэш
                classificationCache[remoteFile.path] = result
                _classificationState.value = ClassificationState.Result(remoteFile, result)
            } catch (e: Exception) {
                _classificationState.value = ClassificationState.Error(
                    remoteFile,
                    "Ошибка: ${e.message}"
                )
            }
        }
    }

    /**
     * Заглушка для тестирования (в реальном проекте заменить на реальную классификацию)
     */
    private suspend fun simulateClassification(remoteFile: RemoteFile): FileClassifier.ClassificationResult {
        // Временная заглушка для тестирования
        // В реальном проекте здесь будет вызов fileClassifier.classifyFile()

        return when {
            remoteFile.name.endsWith(".jpg", ignoreCase = true) ||
                    remoteFile.name.endsWith(".png", ignoreCase = true) ->
                FileClassifier.ClassificationResult(
                    category = "Изображение",
                    confidence = 0.9f,
                    emoji = "📷",
                    details = "Графический файл"
                )

            remoteFile.name.endsWith(".txt", ignoreCase = true) ||
                    remoteFile.name.endsWith(".md", ignoreCase = true) ->
                FileClassifier.ClassificationResult(
                    category = "Текстовый файл",
                    confidence = 0.8f,
                    emoji = "📄",
                    details = "Текстовый документ"
                )

            remoteFile.name.endsWith(".pdf", ignoreCase = true) ||
                    remoteFile.name.endsWith(".doc", ignoreCase = true) ->
                FileClassifier.ClassificationResult(
                    category = "Документ",
                    confidence = 0.85f,
                    emoji = "📑",
                    details = "Файл документа"
                )

            else -> FileClassifier.ClassificationResult(
                category = "Файл",
                confidence = 0.7f,
                emoji = "📎",
                details = "Файл .${remoteFile.name.substringAfterLast(".", "")}"
            )
        }
    }

    /**
     * Очистка кэша при смене директории
     */
    fun clearClassificationCache() {
        classificationCache.clear()
        _classificationState.value = ClassificationState.Idle
    }

    sealed class ClassificationState {
        data object Idle : ClassificationState()
        data class Loading(val file: RemoteFile) : ClassificationState()
        data class Result(val file: RemoteFile, val result: FileClassifier.ClassificationResult) : ClassificationState()
        data class Error(val file: RemoteFile, val message: String) : ClassificationState()
    }
}