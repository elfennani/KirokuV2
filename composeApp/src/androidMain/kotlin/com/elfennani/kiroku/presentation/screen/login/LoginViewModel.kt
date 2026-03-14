package com.elfennani.kiroku.presentation.screen.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.elfennani.kiroku.Constants

@SuppressLint("StaticFieldLeak")
class LoginViewModel(
    private val context: Context
) : ViewModel() {
    fun initiateLogin() {
        val clientID = Constants.clientId
        val loginUrl =
            "https://anilist.co/api/v2/oauth/authorize?client_id=$clientID&response_type=token"

        val intent = Intent(Intent.ACTION_VIEW, loginUrl.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}