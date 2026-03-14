package com.elfennani.kiroku.presentation.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elfennani.kiroku.presentation.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen() {
    val viewModel = koinViewModel<LoginViewModel>()
    LoginScreen(
        onLoginInitiated = {
            viewModel.initiateLogin()
        }
    )
}

@Composable
private fun LoginScreen(
    onLoginInitiated: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxSize()
                    .padding(24.dp)
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    "All Your Anime & Manga in One Place",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge,
                    lineHeight = MaterialTheme.typography.headlineLarge.fontSize * 1.2,
                    letterSpacing = (-1).sp
                )
                Text(
                    "Keep your lists updated, progress synced, and series organized effortlessly.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.5
                )
                Spacer(Modifier.height(56.dp))
                Button(
                    onClick = onLoginInitiated,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    colors = if (isDarkTheme) ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) else ButtonDefaults.buttonColors()
                ) {
                    Spacer(Modifier.width(16.dp))
                    Text("Connect AniList Account")
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    AppTheme(darkTheme = false) {
        LoginScreen(onLoginInitiated = {}, isDarkTheme = false)
    }
}

@Preview
@Composable
private fun LoginScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        LoginScreen(onLoginInitiated = {}, isDarkTheme = true)
    }
}