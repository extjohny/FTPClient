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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import ru.abdulkhalikov.ftpclient.di.ApplicationComponent
import ru.abdulkhalikov.ftpclient.di.DaggerApplicationComponent
import ru.abdulkhalikov.ftpclient.di.DataModule
import ru.abdulkhalikov.ftpclient.presentation.navigation.AppNavGraph
import ru.abdulkhalikov.ftpclient.presentation.navigation.Destination
import ru.abdulkhalikov.ftpclient.presentation.ui.connection.ConnectionScreen
import ru.abdulkhalikov.ftpclient.presentation.ui.connection.ConnectionViewModel
import ru.abdulkhalikov.ftpclient.presentation.ui.files.FilesScreen
import ru.abdulkhalikov.ftpclient.presentation.ui.files.FilesViewModel
import ru.abdulkhalikov.ftpclient.presentation.ui.theme.FTPClientTheme

class MainActivity : ComponentActivity() {

    lateinit var component: ApplicationComponent

    lateinit var viewModelFactory: ViewModelProvider.Factory


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        component = DaggerApplicationComponent.builder()
            .dataModule(DataModule(this))
            .build()

        viewModelFactory = component.viewModelFactory()

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
                            val connectionViewModel: ConnectionViewModel = viewModel(
                                factory = viewModelFactory
                            )
                            ConnectionScreen(
                                viewModel = connectionViewModel,
                                modifier = Modifier.padding(paddingValues),
                                onSuccessConnection = {
                                    navController.navigate(Destination.Files.route)
                                }
                            )
                        },
                        filesScreen = {
                            val filesViewModel: FilesViewModel = viewModel(
                                factory = viewModelFactory
                            )
                            FilesScreen(
                                viewModel = filesViewModel
                            )
                        }
                    )
                }
            }
        }
    }
}