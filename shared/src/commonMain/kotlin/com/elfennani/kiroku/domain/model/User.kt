package com.elfennani.kiroku.domain.model

data class User(
    val id: Int,
    val name: String,
    /** URL to the user's low resolution avatar */
    val icon: String?,
    /** URL to the user's high resolution avatar */
    val avatar: String?,
)
