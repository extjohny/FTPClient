package ru.abdulkhalikov.ftpclient.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPClient
import java.io.IOException

object FTPConnectionManager {

    private val instance: FTPClient? = null

    suspend fun get(): FTPClient {
        return withContext(Dispatchers.Main.immediate) {
            val ftpClient = FTPClient().also {
                instance == it
            }
            if (ftpClient.connect()) {
                ftpClient
            } else {
                throw IOException("FTPClient connection is not open")
            }
        }
    }

    private suspend fun FTPClient.connect(

    ): Boolean {
        return withContext(Dispatchers.IO) {
            this@connect.connect("31.24.251.233", 21)
            this@connect.login("user311585", "DzKp2Xpn1a16")
            this@connect.isConnected
        }
    }
}