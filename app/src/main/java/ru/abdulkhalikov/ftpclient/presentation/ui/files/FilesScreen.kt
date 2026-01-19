package ru.abdulkhalikov.ftpclient.presentation.ui.files

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.abdulkhalikov.ftpclient.R
import ru.abdulkhalikov.ftpclient.domain.GetFTPFilesStatus
import ru.abdulkhalikov.ftpclient.domain.RemoteFile
import ru.abdulkhalikov.ftpclient.domain.UploadFilesStatus
import ru.abdulkhalikov.ftpclient.presentation.navigation.Destination
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: FilesViewModel,
    navController: NavController? = null
) {
    val screenState by viewModel.screenState.collectAsState()
    val currentPathState by viewModel.remoteCurrentPath.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
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

                else -> {}
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (currentState.files.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No files yet",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(
                                "Add a file or create a folder to get started",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn {
                            items(items = currentState.files, key = { it.id }) { file ->
                                FTPFile(
                                    ftpFile = file,
                                    onFileClick = { viewModel.navigateToDirectory(file) },
                                    onRemoveClick = { viewModel.removeFile(file) }
                                )
                            }
                        }
                    }
                    if (uploadState == UploadFilesStatus.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
private fun FTPFile(
    ftpFile: RemoteFile,
    onFileClick: (RemoteFile) -> Unit,
    onRemoveClick: (RemoteFile) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = showInfoDialog || showDeleteDialog || menuExpanded) {
        when {
            showInfoDialog -> {
                showInfoDialog = false
            }
            showDeleteDialog -> {
                showDeleteDialog = false
            }
            menuExpanded -> {
                menuExpanded = false
            }
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
    ) {
        Row {
            Image(
                modifier = Modifier.size(50.dp),
                painter = if (ftpFile.isDirectory) painterResource(R.drawable.directory) else painterResource(
                    R.drawable.file
                ),
                contentDescription = null
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
                        text = { Text("About") },
                        onClick = {
                            menuExpanded = false
                            showInfoDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showInfoDialog) {
        val typeText = if (ftpFile.isDirectory) "Folder" else "File"
        val sizeText = if (ftpFile.isDirectory) "-" else ftpFile.formattedSize
        val lastModifiedText = ftpFile.formattedDate ?: "-"
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("About") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(label = "Name", value = ftpFile.name)
                    InfoRow(label = "Type", value = typeText)
                    InfoRow(label = "Size", value = sizeText)
                    InfoRow(label = "Modified at", value = lastModifiedText)
                    InfoRow(label = "Path", value = ftpFile.path)
                    InfoRow(
                        label = "ID",
                        value = NumberFormat.getIntegerInstance(Locale.getDefault())
                            .format(ftpFile.id)
                    )
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
            title = { Text("You sure?") },
            text = { Text("Delete \"${ftpFile.name}\"? It's irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onRemoveClick(ftpFile)
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
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