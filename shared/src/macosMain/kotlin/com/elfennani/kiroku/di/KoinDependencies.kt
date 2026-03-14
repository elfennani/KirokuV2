package com.elfennani.kiroku.di

import com.elfennani.kiroku.data.datasource.AllAnimeDataSource
import com.elfennani.kiroku.domain.usecase.FetchOnGoingMedia
import com.elfennani.kiroku.domain.usecase.GetOnGoingMedia
import com.elfennani.kiroku.domain.usecase.GetSession
import com.elfennani.kiroku.domain.usecase.GetViewer
import com.elfennani.kiroku.domain.usecase.SaveSession
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object KoinDependencies: KoinComponent {
    fun getSessionUseCase(): GetSession {
        return getKoin().get()
    }

    val saveSessionUseCase by inject<SaveSession>()
    val getViewerUseCase by inject<GetViewer>()

    val allAnimeDataSource by inject<AllAnimeDataSource>()

    val getOnGoingMediaUseCase by inject<GetOnGoingMedia>()
    val fetchOnGoingMediaUseCase by inject<FetchOnGoingMedia>()
}