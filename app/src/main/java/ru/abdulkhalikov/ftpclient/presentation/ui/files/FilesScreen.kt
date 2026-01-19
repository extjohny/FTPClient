package ru.abdulkhalikov.ftpclient.presentation.ui.files

import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.abdulkhalikov.ftpclient.R
import ru.abdulkhalikov.ftpclient.domain.GetFTPFilesStatus
import ru.abdulkhalikov.ftpclient.domain.RemoteFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    onAddFileButtonClick: () -> Unit
) {
    val viewModel: FilesViewModel = viewModel()
    val screenState = viewModel.screenState.collectAsState()
    val currentPathState = viewModel.remoteCurrentPath.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentPathState.value) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAddFileButtonClick()
                }
            ) {
                Image(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    ) { paddingValues ->
        when (val currentState = screenState.value) {
            is GetFTPFilesStatus.Error -> {
                val context = LocalContext.current
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
                LazyColumn(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    items(items = currentState.files, key = { it.id }) {
                        FTPFile(it) {
                            viewModel.navigateToDirectory(it)
                        }
                    }
                    item {

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