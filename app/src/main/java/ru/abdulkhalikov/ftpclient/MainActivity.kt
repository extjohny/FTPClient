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
import androidx.navigation.compose.rememberNavController
import ru.abdulkhalikov.ftpclient.presentation.navigation.AppNavGraph
import ru.abdulkhalikov.ftpclient.presentation.navigation.Destination
import ru.abdulkhalikov.ftpclient.presentation.ui.connection.ConnectionScreen
import ru.abdulkhalikov.ftpclient.presentation.ui.files.FilesScreen
import ru.abdulkhalikov.ftpclient.presentation.ui.theme.FTPClientTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            FTPClientTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("FTP Client") }
                        )
                    }
                ) { paddingValues ->
                    AppNavGraph(
                        navController = navController,
                        connectionScreen = {
                            ConnectionScreen(
                                modifier = Modifier.padding(paddingValues),
                                onSuccessConnection = {
                                    navController.navigate(Destination.Files.route)
                                }
                            )
                        },
                        filesScreen = {
                            FilesScreen {
                                // on add lick logic
                            }
                        }
                    )
                }
            }
        }
    }
}