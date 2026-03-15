package com.elfennani.kiroku.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elfennani.kiroku.domain.util.ExternalUriHandler
import com.elfennani.kiroku.domain.util.GlobalErrorHandler
import com.elfennani.kiroku.presentation.screen.auth.AuthRoute
import com.elfennani.kiroku.presentation.screen.auth.AuthScreen
import com.elfennani.kiroku.presentation.screen.home.HomeRoute
import com.elfennani.kiroku.presentation.screen.home.HomeScreen
import com.elfennani.kiroku.presentation.screen.login.LoginRoute
import com.elfennani.kiroku.presentation.screen.login.LoginScreen
import com.elfennani.kiroku.presentation.screen.match.MatchRoute
import com.elfennani.kiroku.presentation.screen.match.MatchScreen
import com.elfennani.kiroku.presentation.screen.media.MediaRoute
import com.elfennani.kiroku.presentation.screen.media.MediaScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation(isAuthed: Boolean) {
    val viewModel = koinViewModel<NavigationViewModel>(
        parameters = { parametersOf(isAuthed) }
    )
    val backStack by viewModel.backstack.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        GlobalErrorHandler.errors.collect { message ->
            snackbarHostState.showSnackbar(
                message = message.message ?: "Something went wrong!",
                withDismissAction = true
            )
        }
    }

    DisposableEffect(Unit) {
        ExternalUriHandler.listener = { uri ->
            if (uri.startsWith("kiroku://redirect")) {
                val token = uri.substringAfter("access_token=").substringBefore("&")
                viewModel.navigateTo(AuthRoute(token))
            }
        }
        onDispose {
            ExternalUriHandler.listener = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        NavDisplay(
            entryDecorators = listOf<NavEntryDecorator<Any>>(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            backStack = backStack,
            onBack = { viewModel.back() },
            predictivePopTransitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn() + scaleIn(initialScale = 0.9f),
                    initialContentExit = fadeOut() + slideOutHorizontally(
                        targetOffsetX = { it / 2 },
                    ) + scaleOut(targetScale = 0.9f)
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn() + scaleIn(initialScale = 0.9f),
                    initialContentExit = fadeOut() + slideOutHorizontally(
                        targetOffsetX = { it / 2 },
                    ) + scaleOut(targetScale = 0.9f)
                )
            },
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn() + slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    ) + scaleIn(initialScale = 0.9f),
                    initialContentExit = fadeOut() + scaleOut(targetScale = 0.9f)
                )
            },
            entryProvider = entryProvider {
                entry<LoginRoute>() {
                    LoginScreen()
                }
                entry<AuthRoute> {
                    AuthScreen(
                        route = it,
                        onResetNavigationToRoute = viewModel::resetTo
                    )
                }
                entry<HomeRoute> {
                    HomeScreen(
                        onNavigate = viewModel::navigateTo
                    )
                }
                entry<MediaRoute> {
                    MediaScreen(
                        route = it,
                        onNavigate = viewModel::navigateTo,
                        onNavigateBack = viewModel::back
                    )
                }
                entry<MatchRoute> {
                    MatchScreen(
                        route = it,
                        onNavigateBack = viewModel::back,
                    )
                }
            }
        )
    }
}
