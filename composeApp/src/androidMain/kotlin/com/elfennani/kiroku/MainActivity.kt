package com.elfennani.kiroku

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.elfennani.kiroku.domain.usecase.GetSession
import com.elfennani.kiroku.domain.util.ExternalUriHandler
import com.elfennani.kiroku.presentation.screen.Navigation
import com.elfennani.kiroku.presentation.theme.AppTheme
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val getSession: GetSession = get()
        val session = runBlocking { getSession() }

        setContent {
            AppTheme() {
                Navigation(isAuthed = session != null)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Toast.makeText(
            this,
            "Received new intent: $intent",
            Toast.LENGTH_SHORT
        ).show()
        handleIntent(intent)
    }

    fun handleIntent(intent: Intent) {
        intent.data?.let {
            Toast.makeText(
                this,
                "Received deep link: $it",
                Toast.LENGTH_SHORT
            ).show()
            ExternalUriHandler.onNewUri(it.toString())
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}