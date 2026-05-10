package com.contest.memocut

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val screens = mutableStateListOf<@Composable () -> Unit>({ HomeScreen(this) })
    val screen get() = screens.lastOrNull()

    fun pop() {
        screens.removeLastOrNull()
    }

    fun push(s: @Composable () -> Unit) {
        screens += s
    }
}