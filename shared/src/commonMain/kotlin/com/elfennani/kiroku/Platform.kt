package com.elfennani.kiroku

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform