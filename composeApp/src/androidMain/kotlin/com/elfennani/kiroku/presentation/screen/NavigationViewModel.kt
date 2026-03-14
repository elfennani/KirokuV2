package com.elfennani.kiroku.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import com.elfennani.kiroku.presentation.screen.home.HomeRoute
import com.elfennani.kiroku.presentation.screen.login.LoginRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class NavigationViewModel(
    private val isAuthed: Boolean
) : ViewModel() {
    val backstack = MutableStateFlow<List<NavKey>>(listOf(if (isAuthed) HomeRoute else LoginRoute))

    fun navigateTo(route: NavKey) {
        backstack.update {
            it + route
        }
    }

    fun back() {
        backstack.update {
            if (it.size > 1) {
                it.dropLast(1)
            } else {
                it
            }
        }
    }

    fun resetTo(route: NavKey) {
        backstack.update {
            listOf(route)
        }
    }
}