package ru.abdulkhalikov.ftpclient.data.network

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPSClient
import ru.abdulkhalikov.ftpclient.domain.ProtocolType

object FTPConnectionManager {

    private var currentClient: FTPClient? = null
    private val lock = Any()

    fun getFTPClient(protocolType: ProtocolType = ProtocolType.FTP): FTPClient {
        synchronized(lock) {
            currentClient?.let {
                try {
                    if (it.isConnected) {
                        it.disconnect()
                    }
                } catch (e: Exception) {

                }
            }

            currentClient = when (protocolType) {
                ProtocolType.FTP -> FTPClient()
                ProtocolType.FTPS -> FTPSClient(true)
            }

            return currentClient!!
        }
    }

    fun disconnect() {
        synchronized(lock) {
            try {
                currentClient?.let {
                    if (it.isConnected) {
                        it.disconnect()
                    }
                }
            } catch (e: Exception) {
            }
            currentClient = null
        }
    }
}