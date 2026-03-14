package com.elfennani.kiroku.presentation.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.elfennani.kiroku.presentation.screen.home.HomeRoute
import com.elfennani.kiroku.presentation.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AuthScreen(
    route: AuthRoute,
    onResetNavigationToRoute: (NavKey) -> Unit
) {
    val viewModel = koinViewModel<AuthViewModel>(
        parameters = { parametersOf(route.token) }
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthUiState.Success) {
            onResetNavigationToRoute(HomeRoute)
        }
    }

    AuthScreen(state = state)
}

@Composable
private fun AuthScreen(
    state: AuthUiState
) {
    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                is AuthUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                AuthUiState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Authenticating...", textAlign = TextAlign.Center)
                }

                AuthUiState.Success -> {
                    Text(text = "Authentication successful!", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Preview
@Composable
private fun AuthScreenPreview() {
    AppTheme {
        AuthScreen(state = AuthUiState.Loading)
    }
}

@Preview
@Composable
private fun AuthScreenErrorPreview() {
    AppTheme {
        AuthScreen(state = AuthUiState.Error(message = "An error occurred during authentication."))
    }
}

@Preview
@Composable
private fun AuthScreenSuccessPreview() {
    AppTheme {
        AuthScreen(state = AuthUiState.Success)
    }
}