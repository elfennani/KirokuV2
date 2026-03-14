package com.elfennani.kiroku.domain.model

data class Session(
    val token: String,
    val userID: Int?
)
