package ru.abdulkhalikov.ftpclient.di

import dagger.Component
import ru.abdulkhalikov.ftpclient.MainActivity
import ru.abdulkhalikov.ftpclient.presentation.ui.ViewModelFactory

@Component(modules = [DataModule::class, DomainModule::class, ViewModelModule::class])
interface ApplicationComponent {

    fun inject(activity: MainActivity)

    fun viewModelFactory(): ViewModelFactory
}