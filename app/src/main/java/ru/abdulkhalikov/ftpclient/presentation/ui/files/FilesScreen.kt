package ru.abdulkhalikov.ftpclient.presentation.ui.files

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ru.abdulkhalikov.ftpclient.ai.FileClassifier
import ru.abdulkhalikov.ftpclient.domain.GetFTPFilesStatus
import ru.abdulkhalikov.ftpclient.domain.RemoteFile
import ru.abdulkhalikov.ftpclient.domain.UploadFilesStatus
import ru.abdulkhalikov.ftpclient.presentation.navigation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: FilesViewModel,
    navController: NavController? = null
) {
    val screenState by viewModel.screenState.collectAsState()
    val currentPathState by viewModel.remoteCurrentPath.collectAsState()
    val uploadState = viewModel.uploadState.collectAsState()
    val createDirectoryResult by viewModel.createDirectoryResult.collectAsState()
    val canNavigateBack by remember(currentPathState) {
        derivedStateOf { viewModel.canNavigateBack() }
    }
    val context = LocalContext.current

    var showCreateDirectoryDialog by remember { mutableStateOf(false) }
    var directoryName by remember { mutableStateOf("") }
    var showFABMenu by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        when {
            showCreateDirectoryDialog -> {
                showCreateDirectoryDialog = false
                directoryName = ""
            }

            showFABMenu -> {
                showFABMenu = false
            }

            canNavigateBack -> {
                viewModel.navigateBack()
            }

            else -> {
                navController?.popBackStack(
                    route = Destination.Connection.route,
                    inclusive = false
                ) ?: run {

                }
            }
        }
    }

    LaunchedEffect(createDirectoryResult) {
        createDirectoryResult?.let { result ->
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            viewModel.clearCreateDirectoryResult()
            if (result.contains("successfully", ignoreCase = true)) {
                showCreateDirectoryDialog = false
                directoryName = ""
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = AddFileContract()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {

            }
            viewModel.addFile(it)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(currentPathState, color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (showFABMenu) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            showCreateDirectoryDialog = true
                            showFABMenu = false
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null
                            )
                        },
                        text = { Text("Create Folder") }
                    )
                    ExtendedFloatingActionButton(
                        onClick = {
                            filePickerLauncher.launch()
                            showFABMenu = false
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        },
                        text = { Text("Add File") }
                    )
                }
            } else {
                FloatingActionButton(
                    onClick = { showFABMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        }
    ) { paddingValues ->
        LaunchedEffect(uploadState) {
            when (val state = uploadState) {
                is UploadFilesStatus.Error -> {
                    Toast.makeText(
                        context,
                        "Downloading error: ${state.error}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is UploadFilesStatus.Success -> {
                    Toast.makeText(
                        context,
                        "File successfully added",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {

                }
            }
        }

        when (val currentState = screenState) {
            is GetFTPFilesStatus.Error -> {
                Toast.makeText(
                    context,
                    currentState.error,
                    Toast.LENGTH_SHORT
                ).show()
            }

            GetFTPFilesStatus.Initial -> {}
            GetFTPFilesStatus.Loading -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is GetFTPFilesStatus.Success -> {
                FTPFiles(
                    paddingValues = paddingValues,
                    files = currentState.files,
                    viewModel = viewModel,
                    uploadState = uploadState
                )
            }
        }

        if (showCreateDirectoryDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCreateDirectoryDialog = false
                    directoryName = ""
                },
                title = { Text("Create Folder") },
                text = {
                    OutlinedTextField(
                        value = directoryName,
                        onValueChange = { directoryName = it },
                        label = { Text("Folder Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (directoryName.isNotBlank()) {
                                viewModel.createDirectory(directoryName)
                                showCreateDirectoryDialog = false
                                directoryName = ""
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCreateDirectoryDialog = false
                            directoryName = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun FTPFiles(
    paddingValues: PaddingValues,
    files: List<RemoteFile>,
    uploadState: State<UploadFilesStatus>,
    viewModel: FilesViewModel, // Добавляем viewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (files.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Файлов нет",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    "Добавьте файл или создайте папку",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(items = files, key = { it.id }) { file ->
                    FTPFile(
                        ftpFile = file,
                        onFileClick = { viewModel.navigateToDirectory(file) },
                        onRemoveClick = { viewModel.removeFile(file) },
                        viewModel = viewModel // Передаем viewModel
                    )
                }
            }
        }
        if (uploadState.value == UploadFilesStatus.Loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Функция для получения эмодзи по умолчанию
private fun getDefaultEmoji(fileName: String): String {
    return when {
        fileName.endsWith(".jpg", ignoreCase = true) ||
                fileName.endsWith(".png", ignoreCase = true) ||
                fileName.endsWith(".gif", ignoreCase = true) -> "🖼️"

        fileName.endsWith(".txt", ignoreCase = true) ||
                fileName.endsWith(".md", ignoreCase = true) -> "📄"

        fileName.endsWith(".pdf", ignoreCase = true) ||
                fileName.endsWith(".doc", ignoreCase = true) ||
                fileName.endsWith(".docx", ignoreCase = true) -> "📑"

        fileName.endsWith(".xls", ignoreCase = true) ||
                fileName.endsWith(".xlsx", ignoreCase = true) ||
                fileName.endsWith(".csv", ignoreCase = true) -> "📊"

        fileName.endsWith(".zip", ignoreCase = true) ||
                fileName.endsWith(".rar", ignoreCase = true) -> "🗜️"

        fileName.endsWith(".mp3", ignoreCase = true) ||
                fileName.endsWith(".wav", ignoreCase = true) -> "🎵"

        fileName.endsWith(".mp4", ignoreCase = true) ||
                fileName.endsWith(".avi", ignoreCase = true) -> "🎬"

        fileName.contains("java", ignoreCase = true) ||
                fileName.contains("kt", ignoreCase = true) ||
                fileName.contains("py", ignoreCase = true) -> "💻"

        else -> "📄"
    }
}

@Composable
private fun FTPFile(
    ftpFile: RemoteFile,
    onFileClick: (RemoteFile) -> Unit,
    onRemoveClick: (RemoteFile) -> Unit,
    viewModel: FilesViewModel? = null // Добавляем viewModel
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAIDialog by remember { mutableStateOf(false) }

    val classificationState by viewModel?.classificationState?.collectAsState()
        ?: remember { mutableStateOf(FilesViewModel.ClassificationState.Idle) }
    val aiResult = remember { mutableStateOf<FileClassifier.ClassificationResult?>(null) }

    // Обновляем AI результат при изменении состояния
    LaunchedEffect(classificationState) {
        when (val state = classificationState) {
            is FilesViewModel.ClassificationState.Result -> {
                if (state.file.id == ftpFile.id) {
                    aiResult.value = state.result
                }
            }

            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(20.dp)
            .clickable(enabled = ftpFile.isDirectory) {
                onFileClick(ftpFile)
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // Запускаем анализ ИИ при долгом нажатии
                        viewModel?.classifyFile(ftpFile)
                        showAIDialog = true
                    }
                )
            }
    ) {
        Row {
            // Вместо изображения используем Text с эмодзи
            Text(
                text = if (ftpFile.isDirectory) "📁" else
                    aiResult.value?.emoji ?: getDefaultEmoji(ftpFile.name),
                fontSize = 32.sp,
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(25.dp))
            Column {
                Text(
                    ftpFile.name,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    ftpFile.formattedSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Показываем результат ИИ под именем файла
                if (aiResult.value != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ИИ: ${aiResult.value!!.category}",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1F))
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "File menu"
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Анализ ИИ") },
                        onClick = {
                            menuExpanded = false
                            viewModel?.classifyFile(ftpFile)
                            showAIDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Информация") },
                        onClick = {
                            menuExpanded = false
                            showInfoDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Удалить") },
                        onClick = {
                            menuExpanded = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    // Диалог с информацией ИИ
    if (showAIDialog) {
        AlertDialog(
            onDismissRequest = { showAIDialog = false },
            title = { Text("Анализ ИИ") },
            text = {
                when (val state = classificationState) {
                    is FilesViewModel.ClassificationState.Loading -> {
                        if (state.file.id == ftpFile.id) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("ИИ анализирует файл...")
                            }
                        }
                    }

                    is FilesViewModel.ClassificationState.Result -> {
                        if (state.file.id == ftpFile.id) {
                            Column {
                                Text("Файл: ${state.file.name}")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Категория: ${state.result.category}")
                                Text("Уверенность: ${(state.result.confidence * 100).toInt()}%")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Подробности:")
                                Text(state.result.details)
                            }
                        }
                    }

                    is FilesViewModel.ClassificationState.Error -> {
                        if (state.file.id == ftpFile.id) {
                            Text("Ошибка: ${state.message}")
                        }
                    }

                    else -> Text("Нажмите и удерживайте файл для анализа")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAIDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showInfoDialog) {
        val typeText = if (ftpFile.isDirectory) "Папка" else "Файл"
        val sizeText = if (ftpFile.isDirectory) "-" else ftpFile.formattedSize
        val lastModifiedText = ftpFile.formattedDate ?: "-"
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Информация") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(label = "Имя", value = ftpFile.name)
                    InfoRow(label = "Тип", value = typeText)
                    InfoRow(label = "Размер", value = sizeText)
                    InfoRow(label = "Изменен", value = lastModifiedText)
                    InfoRow(label = "Путь", value = ftpFile.path)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Подтверждение") },
            text = { Text("Удалить \"${ftpFile.name}\"? Это действие необратимо.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onRemoveClick(ftpFile)
                    }
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            modifier = Modifier.width(92.dp)
        )
        Text(text = value)
    }
}