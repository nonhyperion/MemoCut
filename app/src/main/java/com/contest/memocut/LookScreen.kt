package com.contest.memocut

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import kotlinx.coroutines.Runnable
import kotlin.math.min

@Composable
fun LookScreen(model: MainViewModel, videoUri: Uri, stickers: List<Sticker>) {
    val context = LocalContext.current
    var videoDuration by remember { mutableIntStateOf(1) }
    var position by remember { mutableIntStateOf(0) }
    var videoView: VideoView? by remember { mutableStateOf(null) }
    var videoViewSize by remember { mutableStateOf(IntSize.Zero) }
    var isPlaying by remember { mutableStateOf(false) }
    var isEnd by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    if (isEnd)
        AlertDialog(
            {},
            {
                IconButton(
                    { model.push { AnsScreen(model, videoUri, stickers) } },
                    Modifier.width(100.dp)
                ) { Text("開始答題") }
            },
            dismissButton = {
                IconButton({
                    isEnd = false
                    videoView?.seekTo(0)
                    videoView?.start()
                    isPlaying = true
                }, Modifier.width(100.dp)) { Text("重新播放") }
            },
            text = { Text("播放結束") })

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(indication = null, interactionSource = null) {
                videoView?.pause()
                isPlaying = false
            },
        contentAlignment = Alignment.Center
    ) {
        Box() {
            for (i in stickers) {
                if (position in i.time..(i.time + 5000))
                    UriImage(
                        i.uri, context, null, modifier = Modifier
                            .size(with(density) {
                                val maxPx = min(videoViewSize.width, videoViewSize.height)

                                val maxDp = if (maxPx > 0) {
                                    (maxPx / with(density) { 1.dp.toPx() }).dp
                                } else {
                                    Dp.Infinity // 或直接跳過
                                }
                                (150.dp * i.scale.value).coerceIn(
                                    1.dp,
                                    maxDp
                                )
                            })
                            .graphicsLayer {
                                translationX = i.offset.value.x * videoViewSize.width
                                translationY = i.offset.value.y * videoViewSize.height
                            }
                            .zIndex(1f)
                    )
            }
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        setVideoURI(videoUri)
                        setOnPreparedListener {
                            videoDuration = duration
                            videoView = this
                            val handler = Handler(Looper.getMainLooper())
                            val runnable = object : Runnable {
                                override fun run() {
                                    position = currentPosition
                                    handler.postDelayed(this, 10)
                                }
                            }
                            handler.post(runnable)
                        }
                        setOnCompletionListener {
                            isPlaying = false
                            isEnd = true
                        }
                    }
                }, modifier = Modifier.onSizeChanged {
                    videoViewSize = it
                }
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(
                !isPlaying,
                enter = slideInVertically { it },
                exit = slideOutVertically(
                    tween(
                        delayMillis = 1000,
                        durationMillis = 1000
                    )
                ) { it }) {
                Row(
                    Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VideoSeekBar(
                        isPlaying,
                        {
                            videoView?.pause()
                            isPlaying = !isPlaying
                        },
                        {
                            videoView?.start()
                            isPlaying = !isPlaying
                        },
                        {
                            videoView?.seekTo((it * videoDuration).toInt())
                        },
                        {
                            videoView?.start()
                            Thread.sleep(200)
                            videoView?.pause()
                        },
                        position / videoDuration.toFloat(),
                        modifier = Modifier.fillMaxWidth(.95f)
                    )
                    Text(
                        "${
                            (position / 60000).toString().padStart(2, '0')
                        }:${(position.toFloat() / 1000 % 60).toInt().toString().padStart(2, '0')}"
                    )
                }
            }
        }
    }
}