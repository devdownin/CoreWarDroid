package com.example.corewar

import android.app.Application
import com.example.corewar.di.commonModule
import com.example.corewar.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CoreWarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CoreWarApp)
            modules(commonModule, platformModule())
        }
    }
}
