package com.elfennani.kiroku.di

import com.elfennani.kiroku.presentation.screen.NavigationViewModel
import com.elfennani.kiroku.presentation.screen.auth.AuthViewModel
import com.elfennani.kiroku.presentation.screen.episode.EpisodeViewModel
import com.elfennani.kiroku.presentation.screen.home.HomeViewModel
import com.elfennani.kiroku.presentation.screen.login.LoginViewModel
import com.elfennani.kiroku.presentation.screen.match.MatchViewModel
import com.elfennani.kiroku.presentation.screen.media.MediaViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val composeModule = module {
    // View Models
    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModel { params -> NavigationViewModel(isAuthed = params.get()) }
    viewModel { params -> AuthViewModel(token = params.get(), get()) }
    viewModel { params ->
        MediaViewModel(
            route = params.get(),
            mediaRepository = get(),
            getMediaItems = get()
        )
    }
    viewModel { params -> MatchViewModel(route = params.get(), mediaRepository = get()) }
    viewModel { params -> EpisodeViewModel(initialRoute = params.get(), mediaRepository = get(), context = get()) }
}