package ru.abdulkhalikov.ftpclient.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(
    navController: NavHostController,
    connectionScreen: @Composable () -> Unit,
    filesScreen: @Composable () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Connection.route
    ) {
        composable(Destination.Connection.route) {
            connectionScreen()
        }
        composable(Destination.Files.route) {
            filesScreen()
        }
    }
}