package ru.abdulkhalikov.ftpclient.data.mapper

import org.apache.commons.net.ftp.FTPFile
import ru.abdulkhalikov.ftpclient.data.network.manage_state.GetFTPFilesResult
import ru.abdulkhalikov.ftpclient.data.network.manage_state.UploadFileResult
import ru.abdulkhalikov.ftpclient.domain.GetFTPFilesStatus
import ru.abdulkhalikov.ftpclient.domain.RemoteFile
import ru.abdulkhalikov.ftpclient.domain.UploadFilesStatus

object FTPResultMapper {

    fun UploadFileResult.toDomain(): UploadFilesStatus {
        return when (this) {
            is UploadFileResult.Error -> UploadFilesStatus.Error(this.error)
            UploadFileResult.Initial -> UploadFilesStatus.Initial
            UploadFileResult.Loading -> UploadFilesStatus.Loading
            UploadFileResult.Success -> UploadFilesStatus.Success
        }
    }

    fun GetFTPFilesResult.toDomain(
        currentPath: String
    ): GetFTPFilesStatus {
        return when (this) {
            is GetFTPFilesResult.Error -> GetFTPFilesStatus.Error(this.error)
            GetFTPFilesResult.Initial -> GetFTPFilesStatus.Initial
            GetFTPFilesResult.Loading -> GetFTPFilesStatus.Loading
            is GetFTPFilesResult.Success -> GetFTPFilesStatus.Success(
                this.files.toDomain(
                    currentPath
                )
            )
        }
    }

    private fun Array<FTPFile?>.toDomain(currentPath: String): List<RemoteFile> {
        val result = mutableListOf<RemoteFile>()
        var id = 0
        for (ftpFile in this) {
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
        return result.sortedWith(
            compareBy(
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