package ru.abdulkhalikov.ftpclient.di

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class DataModule(private val context: Context) {

    @Provides
    fun provideContext(): Context {
        return context
    }
}