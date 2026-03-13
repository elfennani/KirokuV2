package com.elfennani.kiroku

import com.elfennani.shared.anilist.GetViewerQuery

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
        GetViewerQuery()
    }
}