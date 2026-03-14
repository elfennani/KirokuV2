package com.elfennani.kiroku.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun getKtorClient(): HttpClient {
    return HttpClient(OkHttp)
}