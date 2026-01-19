package ru.abdulkhalikov.ftpclient.di

import dagger.Binds
import dagger.Module
import ru.abdulkhalikov.ftpclient.data.repository.ConnectionRepositoryImpl
import ru.abdulkhalikov.ftpclient.data.repository.FTPFilesRepositoryImpl
import ru.abdulkhalikov.ftpclient.domain.ConnectionRepository
import ru.abdulkhalikov.ftpclient.domain.FTPFilesRepository

@Module
interface DomainModule {

    @Binds
    fun bindConnectionRepository(impl: ConnectionRepositoryImpl): ConnectionRepository

    @Binds
    fun bindFTPFilesRepository(impl: FTPFilesRepositoryImpl): FTPFilesRepository
}