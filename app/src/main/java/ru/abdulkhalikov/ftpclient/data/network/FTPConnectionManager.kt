package ru.abdulkhalikov.ftpclient.data.network

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPSClient
import ru.abdulkhalikov.ftpclient.domain.ProtocolType

object FTPConnectionManager {

    private var currentClient: FTPClient? = null

    private val lock = Any()

    fun getFTPClient(protocolType: ProtocolType = ProtocolType.FTP): FTPClient {
        currentClient?.let { return it }
        synchronized(lock) {
            currentClient?.let {
                try {
                    if (it.isConnected) {
                        it.disconnect()
                    }
                } catch (_: Exception) {

                }
            }

            currentClient = when (protocolType) {
                ProtocolType.FTP -> FTPClient()
                ProtocolType.FTPS -> {
                    FTPSClient()
                }
            }

            return currentClient!!
        }
    }
//    private fun configureSSLProtocols(ftpsClient: FTPSClient) {
//        try {
//            val supportedProtocols = ftpsClient.enabledProtocols
//            Log.d("LOG_TAG", "Supported protocols: ${supportedProtocols?.joinToString()}")
//
//            val safeProtocols = mutableListOf<String>()
//
//            val desiredProtocols = listOf("TLSv1.2", "TLSv1.1", "TLSv1")
//
//            for (protocol in desiredProtocols) {
//                if (supportedProtocols?.contains(protocol) == true) {
//                    safeProtocols.add(protocol)
//                }
//            }
//
//            if (safeProtocols.isNotEmpty()) {
//                ftpsClient.enabledProtocols = safeProtocols.toTypedArray()
//                Log.d("LOG_TAG", "Enabled protocols: ${safeProtocols.joinToString()}")
//            }
//
//        } catch (e: Exception) {
//            Log.e("LOG_TAG", "Error configuring SSL protocols: ${e.message}", e)
//        }
//    }
}