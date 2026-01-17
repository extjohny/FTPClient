package ru.abdulkhalikov.ftpclient.data.network

import org.apache.commons.net.ftp.FTPClient

object FTPConnection {

    private val instance: FTPClient? = null

    private val lock = Any()

    fun get(): FTPClient {
        instance?.let { return it }
        synchronized(lock) {
            instance?.let { return it }
            return FTPClient().also { instance == it }
        }
    }
}