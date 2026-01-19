package ru.abdulkhalikov.ftpclient.presentation.ui.files

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.abdulkhalikov.ftpclient.R
import ru.abdulkhalikov.ftpclient.domain.GetFTPFilesStatus
import ru.abdulkhalikov.ftpclient.domain.RemoteFile
import ru.abdulkhalikov.ftpclient.domain.UploadFilesStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: FilesViewModel
) {
    val screenState = viewModel.screenState.collectAsState()
    val currentPathState = viewModel.remoteCurrentPath.collectAsState()
    val uploadState = viewModel.uploadState.collectAsState()
    val context = LocalContext.current

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
        topBar = {
            TopAppBar(
                title = { Text(currentPathState.value) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch()
                }
            ) {
                Image(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    ) { paddingValues ->
        LaunchedEffect(uploadState.value) {
            when (val state = uploadState.value) {
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

        when (val currentState = screenState.value) {
            is GetFTPFilesStatus.Error -> {
                Toast.makeText(
                    context,
                    currentState.error,
                    Toast.LENGTH_SHORT
                ).show()
            }

            GetFTPFilesStatus.Initial -> {}
            GetFTPFilesStatus.Loading -> {
                CircularProgressIndicator()
            }

            is GetFTPFilesStatus.Success -> {
                Box(modifier = Modifier.padding(paddingValues)) {
                    LazyColumn {
                        items(items = currentState.files, key = { it.id }) { file ->
                            FTPFile(file) {
                                viewModel.navigateToDirectory(it)
                            }
                        }
                        item {

                        }
                    }
                    if (uploadState.value == UploadFilesStatus.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FTPFile(
    ftpFile: RemoteFile,
    onFileClick: (RemoteFile) -> Unit
) {
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
                painter = if (ftpFile.isDirectory) painterResource(R.drawable.directory) else painterResource(
                    R.drawable.file
                ),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(25.dp))
            Column {
                Text(ftpFile.name)
                Text(ftpFile.size.toString())
            }
            Spacer(modifier = Modifier.weight(1F))
            Image(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null
            )
        }
    }
}