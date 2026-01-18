package ru.abdulkhalikov.ftpclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import ru.abdulkhalikov.ftpclient.presentation.FilesScreen
import ru.abdulkhalikov.ftpclient.ui.theme.FTPClientTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FTPClientTheme {
                Scaffold(
                   topBar = {
                       TopAppBar(
                           title = { Text("FTP Client") }
                       )
                   }
                ) {
                    FilesScreen(modifier = Modifier.padding(it))
                }
            }
        }
    }
}