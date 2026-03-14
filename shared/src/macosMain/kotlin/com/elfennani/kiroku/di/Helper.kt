package com.elfennani.kiroku.di

import org.koin.core.context.startKoin
import org.koin.core.logger.PrintLogger

fun initKoin(){
    startKoin {
        logger(PrintLogger())
        modules(
            commonModule,
            platformModule
        )
    }
}