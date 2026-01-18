package ru.abdulkhalikov.ftpclient.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.abdulkhalikov.ftpclient.R
import ru.abdulkhalikov.ftpclient.domain.RemoteFile

@Composable
fun FilesScreen(
    modifier: Modifier = Modifier,
    viewModel: FilesViewModel = viewModel()
) {
    val screenState = viewModel.screenState.collectAsState(FilesScreenState.Initial)

    when (val currentState = screenState.value) {
        is FilesScreenState.Error -> {}
        FilesScreenState.Initial -> {}
        FilesScreenState.Loading -> {}
        is FilesScreenState.Success -> {
            LazyColumn(
                modifier = modifier.fillMaxSize()
            ) {
                items(items = currentState.files, key = { it.id }) {
                    FTPFile(it)
                }
            }
        }
    }
}

@Composable
private fun FTPFile(
    ftpFile: RemoteFile
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(20.dp)
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