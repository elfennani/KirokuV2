package com.elfennani.kiroku.di

import com.elfennani.kiroku.data.local.AppDatabase
import com.elfennani.kiroku.data.local.getDatabaseBuilder
import com.elfennani.kiroku.data.local.getRoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single<AppDatabase> {
            getRoomDatabase(getDatabaseBuilder(androidContext()))
        }
    }