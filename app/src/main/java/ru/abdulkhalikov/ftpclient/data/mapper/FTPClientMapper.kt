package ru.abdulkhalikov.ftpclient.data.mapper

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import ru.abdulkhalikov.ftpclient.domain.RemoteFile

class FTPClientMapper {

    fun mapFtpFilesToRemoteFiles(
        list: Array<FTPFile?>,
        ftpClient: FTPClient = ru.abdulkhalikov.ftpclient.data.network.FTPClient.create()
    ): List<RemoteFile> {
        val result = mutableListOf<RemoteFile>()
        for (i in list) {
            i?.let {
                result.add(
                    RemoteFile(
                        name = it.name,
                        type = it.type,
                        size = it.size,
                        timestamp = it.timestamp,
                        path = getFilePath(
                            ftpFile = it,
                            ftpClient = ftpClient
                        )
                    )
                )
            }
        }
        return result
    }

    private fun getFilePath(ftpFile: FTPFile, ftpClient: FTPClient): String {
        var remoteDirectory = ftpClient.printWorkingDirectory()
        if (!remoteDirectory.endsWith("/")) {
            remoteDirectory += "/"
        }
        return remoteDirectory + ftpFile.name
    }
}