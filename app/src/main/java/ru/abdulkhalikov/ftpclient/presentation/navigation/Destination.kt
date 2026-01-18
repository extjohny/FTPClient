package ru.abdulkhalikov.ftpclient.presentation.navigation

sealed class Destination(val route: String) {

    data object Connection : Destination(CONNECTION_ROUTE)

    data object Files : Destination(FILES_ROUTE)

    companion object {

        private const val CONNECTION_ROUTE = "connection"

        private const val FILES_ROUTE = "files"
    }
}