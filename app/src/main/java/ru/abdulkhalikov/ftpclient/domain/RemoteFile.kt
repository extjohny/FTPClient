package ru.abdulkhalikov.ftpclient.domain

import org.apache.commons.net.ftp.FTPFile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class RemoteFile(
    val id: Int,
    val name: String,
    val type: Int,
    val size: Long,
    val timestamp: Calendar?,
    val path: String
) {
    val isDirectory: Boolean
        get() = type == FTPFile.DIRECTORY_TYPE

    val formattedSize: String
        get() = when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }

    val formattedDate: String?
        get() = timestamp?.let {
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(it.time)
        }
}