package com.elfennani.kiroku.di

import com.elfennani.kiroku.data.service.AndroidDownloadService
import com.elfennani.kiroku.data.workers.DownloadWorker
import com.elfennani.kiroku.domain.service.DownloadService
import com.elfennani.kiroku.presentation.screen.NavigationViewModel
import com.elfennani.kiroku.presentation.screen.auth.AuthViewModel
import com.elfennani.kiroku.presentation.screen.debug.DebugViewModel
import com.elfennani.kiroku.presentation.screen.episode.EpisodeViewModel
import com.elfennani.kiroku.presentation.screen.home.HomeViewModel
import com.elfennani.kiroku.presentation.screen.login.LoginViewModel
import com.elfennani.kiroku.presentation.screen.match.MatchViewModel
import com.elfennani.kiroku.presentation.screen.media.MediaViewModel
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val composeModule = module {
    // Services
    single<DownloadService> { AndroidDownloadService(get()) }

    // Workers
    workerOf(::DownloadWorker)

    // View Models
    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::DebugViewModel)
    viewModel { params -> NavigationViewModel(isAuthed = params.get()) }
    viewModel { params -> AuthViewModel(token = params.get(), get()) }
    viewModel { params ->
        MediaViewModel(
            route = params.get(),
            mediaRepository = get(),
            getMediaItems = get(),
            downloadRepository = get()
        )
    }
    viewModel { params -> MatchViewModel(route = params.get(), mediaRepository = get()) }
    viewModel { params ->
        EpisodeViewModel(
            initialRoute = params.get(),
            mediaRepository = get(),
            context = get()
        )
    }
}