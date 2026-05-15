package com.contest.memocut

import android.content.Context
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset

@Composable
fun UriImage(
    uri: Uri, context: Context,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center
) {
    val byteArray =
        context.contentResolver.openInputStream(uri).use { it?.readBytes() }
    byteArray?.let {
        Image(
            BitmapFactory.decodeByteArray(
                it, 0, it.size
            ).asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment
        )
    }
}