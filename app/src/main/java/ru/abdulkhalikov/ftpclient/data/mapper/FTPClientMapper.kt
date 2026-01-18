package ru.abdulkhalikov.ftpclient.data.mapper

import org.apache.commons.net.ftp.FTPFile
import ru.abdulkhalikov.ftpclient.domain.RemoteFile

class FTPClientMapper {

    fun mapFtpFilesToRemoteFiles(
        list: Array<FTPFile?>,
        currentPath: String
    ): List<RemoteFile> {
        val result = mutableListOf<RemoteFile>()
        var id = 0
        for (ftpFile in list) {
            ftpFile?.let { file ->
                if (file.name != "." && file.name != "..") {
                    result.add(
                        RemoteFile(
                            id = id++,
                            name = file.name,
                            type = file.type,
                            size = file.size,
                            timestamp = file.timestamp,
                            path = buildFilePath(currentPath, file.name)
                        )
                    )
                }
            }
        }
        return result.sortedWith(compareBy(
            { !it.isDirectory },
            { it.name.lowercase() }
        ))
    }

    private fun buildFilePath(currentPath: String, fileName: String): String {
        return if (currentPath.endsWith("/")) {
            currentPath + fileName
        } else {
            "$currentPath/$fileName"
        }
    }
}