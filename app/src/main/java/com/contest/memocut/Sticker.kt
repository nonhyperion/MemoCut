package com.contest.memocut

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset

data class Sticker(
    val uri: Uri,
    val time: Int,
    var offset: MutableState<IntOffset>
)
