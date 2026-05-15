package com.contest.memocut

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Runnable

@Composable
fun AskScreen(model: MainViewModel, videoUri: Uri, imageUri: List<Uri>) {
    val context = LocalContext.current
    var videoDuration by remember { mutableIntStateOf(1) }
    var position by remember { mutableIntStateOf(0) }
    var videoView: VideoView? by remember { mutableStateOf(null) }
    var isPlay by remember { mutableStateOf(false) }
    val positionAndTimeImage = remember { mutableStateSetOf<Sticker>() }
    Box(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.width(150.dp)) { }
            Box(Modifier.weight(1f)) {
                Column(Modifier.fillMaxSize()) {
                    AndroidView(factory = { context ->
                        VideoView(context).apply {
                            setVideoURI(videoUri)
                            setOnPreparedListener {
                                videoDuration = duration
                                videoView = this
                                val handler = Handler(Looper.getMainLooper())
                                val runnable = object : Runnable {
                                    override fun run() {
                                        position = currentPosition
                                        handler.postDelayed(this, 500)
                                    }
                                }
                                handler.post(runnable)
                            }
                        }
                    })
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton({
                            if (isPlay)
                                videoView?.pause()
                            else
                                videoView?.start()
                            isPlay = !isPlay
                        }) {
                            if (isPlay)
                                Icon(painterResource(R.drawable.baseline_pause_24), null)
                            else
                                Icon(Icons.Default.PlayArrow, null)
                        }
                        Slider(
                            position.toFloat() / videoDuration.toFloat(),
                            { videoView?.seekTo((it * videoDuration).toInt()) },
                            modifier = Modifier.weight(1f)
                        )
                        Text("${position / 60000}:${position % 60000 / 1000}/${videoDuration / 60000}:${videoDuration % 60000 / 1000}")
                    }
                }
                for (i in positionAndTimeImage) {
                    UriImage(
                        i.uri, context, null, modifier = Modifier
                            .offset { i.offset.value }
                            .pointerInput(Unit) {
                                detectTransformGestures { centroid, pan, zoom, rotation ->
                                    i.offset.value += IntOffset(pan.x.toInt(), pan.y.toInt())

                                }
                            })
                }
            }
            LazyColumn(Modifier.width(150.dp)) {
                items(imageUri) { uri ->
                    var offset by remember { mutableStateOf(IntOffset.Zero) }
                    UriImage(
                        uri,
                        context,
                        null,
                        modifier = Modifier
                            .offset { offset }
                            .pointerInput(Unit) {
                                detectTransformGestures { centroid, pan, zoom, rotation ->
                                    offset += IntOffset(pan.x.toInt(), pan.y.toInt())
                                    if (offset.x < -400)
                                        positionAndTimeImage += Sticker(
                                            uri,
                                            0,
                                            mutableStateOf(IntOffset.Zero)
                                        )
                                }
                            })
                }
            }
        }
    }
}