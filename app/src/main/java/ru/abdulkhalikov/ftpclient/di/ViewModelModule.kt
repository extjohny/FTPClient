package ru.abdulkhalikov.ftpclient.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.abdulkhalikov.ftpclient.presentation.ui.connection.ConnectionViewModel
import ru.abdulkhalikov.ftpclient.presentation.ui.files.FilesViewModel

@Module
interface ViewModelModule {

    @IntoMap
    @ViewModelKey(ConnectionViewModel::class)
    @Binds
    fun bindConnectionViewModel(impl: ConnectionViewModel): ViewModel

    @IntoMap
    @ViewModelKey(FilesViewModel::class)
    @Binds
    fun bindFilesViewModel(impl: FilesViewModel): ViewModel
}