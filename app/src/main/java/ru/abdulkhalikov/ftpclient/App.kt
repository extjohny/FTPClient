package ru.abdulkhalikov.ftpclient

import android.app.Application
import ru.abdulkhalikov.ftpclient.di.DaggerApplicationComponent
import ru.abdulkhalikov.ftpclient.di.DataModule

class MyApp : Application() {

    val appComponent = DaggerApplicationComponent
        .builder()
        .dataModule(DataModule(this))
}