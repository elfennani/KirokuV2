package com.elfennani.kiroku

actual fun getPlatform(): Platform {
    return object : Platform{
        override val name: String
            get() = "MacOS"
    }
}