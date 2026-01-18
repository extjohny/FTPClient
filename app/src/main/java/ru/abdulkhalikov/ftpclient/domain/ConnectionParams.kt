package ru.abdulkhalikov.ftpclient.domain

data class ConnectionParams(
    val host: String,
    val port: Int = 21,
    val username: String,
    val password: String
)