package com.elfennani.kiroku.di

import com.apollographql.apollo.ApolloClient
import com.elfennani.kiroku.data.AniListInterceptor
import com.elfennani.kiroku.data.ApolloClientSource
import com.elfennani.kiroku.data.datasource.AllAnimeDataSource
import com.elfennani.kiroku.data.datasource.AllAnimeSource
import com.elfennani.kiroku.data.datasource.MangaKakalotSource
import com.elfennani.kiroku.data.local.AppDatabase
import com.elfennani.kiroku.data.repository.MediaRepositoryImpl
import com.elfennani.kiroku.data.repository.SessionRepositoryImpl
import com.elfennani.kiroku.data.repository.UserRepositoryImpl
import com.elfennani.kiroku.domain.repository.MediaRepository
import com.elfennani.kiroku.domain.repository.SessionRepository
import com.elfennani.kiroku.domain.repository.UserRepository
import com.elfennani.kiroku.domain.usecase.FetchOnGoingMedia
import com.elfennani.kiroku.domain.usecase.GetMediaItems
import com.elfennani.kiroku.domain.usecase.GetOnGoingMedia
import com.elfennani.kiroku.domain.usecase.GetSession
import com.elfennani.kiroku.domain.usecase.GetViewer
import com.elfennani.kiroku.domain.usecase.SaveSession
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module


val commonModule = module {
    // Apollo Clients
    single(named(ApolloClientSource.AniList)) {
        ApolloClient.Builder()
            .serverUrl("https://graphql.anilist.co")
            .addHttpInterceptor(AniListInterceptor(get()))
            .build()
    }
    single(named(ApolloClientSource.AllAnime)) {
        ApolloClient.Builder()
            .serverUrl("https://api.allanime.day/api")
            .build()
    }

    // Ktor Client
    single {
        HttpClient() {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    // DAOs
    single { get<AppDatabase>().getSessionDao() }
    single { get<AppDatabase>().getUserDao() }
    single { get<AppDatabase>().getMediaDao() }
    single { get<AppDatabase>().getEpisodeDao() }
    single { get<AppDatabase>().getChapterDao() }

    // Sources
    single {
        AllAnimeSource(
            allAnimeClient = get(named(ApolloClientSource.AllAnime)),
            mediaDao = get(),
            httpClient = get()
        )
    }
    singleOf(::MangaKakalotSource)

    // Repositories
    single<SessionRepository> {
        SessionRepositoryImpl(
            userDao = get(),
            sessionDao = get()
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(
            aniListClient = get(named(ApolloClientSource.AniList)),
            userDao = get(),
            sessionDao = get()
        )
    }
    single<MediaRepository> {
        MediaRepositoryImpl(
            allAnimeSource = get(),
            mangaKakalot = get(),
            aniListClient = get(named(ApolloClientSource.AniList)),
            mediaDao = get(),
            getSession = get(),
            episodeDao = get(),
            chapterDao = get(),
        )
    }


    // matched Sources
    single {
        AllAnimeDataSource(
            allAnimeClient = get(named(ApolloClientSource.AllAnime))
        )
    }

    // Use Cases
    single {
        GetSession(get())
    }
    singleOf(::SaveSession)
    singleOf(::GetViewer)
    singleOf(::FetchOnGoingMedia)
    singleOf(::GetOnGoingMedia)
    singleOf(::GetMediaItems)
}