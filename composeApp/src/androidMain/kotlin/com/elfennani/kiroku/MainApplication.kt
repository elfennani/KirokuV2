package com.elfennani.kiroku

import android.app.Application
import com.elfennani.kiroku.di.commonModule
import com.elfennani.kiroku.di.composeModule
import com.elfennani.kiroku.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                commonModule,
                platformModule,
                composeModule
            )
        }
    }
}