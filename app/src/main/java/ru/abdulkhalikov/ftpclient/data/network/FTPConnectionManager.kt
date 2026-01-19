package ru.abdulkhalikov.ftpclient.data.network

import org.apache.commons.net.ftp.FTPClient

object FTPConnectionManager {

    private val instance: FTPClient? = null

    private val lock = Any()

    fun getFTPClient(): FTPClient {
        instance?.let { return it }
        synchronized(lock) {
            instance?.let { return it }
            return FTPClient().also {
                instance == it
            }
        }
    }
}