package com.contest.memocut

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val screens = mutableStateListOf<@Composable () -> Unit>({ HomeScreen(this) })
    val screen get() = screens.lastOrNull()

    var canUseImage = mutableStateListOf<Uri>()
    val stickers = mutableStateListOf<Sticker>()
    var position by mutableIntStateOf(0)
    var videoViewOSize by mutableStateOf(IntSize.Zero)
    var isO by mutableStateOf(0)

    fun pop() {
        screens.removeLastOrNull()
    }

    fun push(s: @Composable () -> Unit) {
        screens += s
    }
}