package ru.abdulkhalikov.ftpclient.domain

interface ConnectionRepository {

    suspend fun connect(params: ConnectionParams)
}