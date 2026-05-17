package com.contest.memocut

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Runnable

@Composable
fun AskScreen(model: MainViewModel, videoUri: Uri, imageUri: List<Uri>) {
    val canUseImage = remember { imageUri.toMutableStateList() }
    val context = LocalContext.current
    var videoDuration by remember { mutableIntStateOf(1) }
    var position by remember { mutableIntStateOf(0) }
    var videoView: VideoView? by remember { mutableStateOf(null) }
    val positionAndTimeImage = remember { mutableStateListOf<Sticker>() }
    var isPlaying by remember { mutableStateOf(false) }
    var addMenu by remember { mutableStateOf(false) }



    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(indication = null, interactionSource = null) {
                videoView?.pause()
                isPlaying = false
            }
    ) {
        for (i in positionAndTimeImage) {
            println(position)
            if (position in i.time..(i.time + 5000))
                UriImage(
                    i.uri, context, null, modifier = Modifier
                        .graphicsLayer {
                            translationX = i.offset.value.x
                            translationY = i.offset.value.y
                            scaleX = i.scale.value
                            scaleY = i.scale.value
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures(true) { centroid, pan, zoom, rotation ->
                                i.scale.value *= zoom
                                i.offset.value += pan * i.scale.value
                            }
                        }
                        .zIndex(.5f)
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
                                handler.postDelayed(this, 500)
                            }
                        }
                        handler.post(runnable)
                    }
                }
            }, modifier = Modifier.fillMaxSize()
        )
        Column(
            Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            AnimatedVisibility(
                !isPlaying,
                enter = slideInVertically { it * -1 },
                exit = slideOutVertically(
                    tween(
                        delayMillis = 1000,
                        durationMillis = 1000
                    )
                ) { it * -1 }) {
                Column() {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(top = 25.dp)
                    ) { }
                    Row(
                        Modifier
                            .background(Color.White)
                            .padding(vertical = 5.dp)
                            .fillMaxWidth(),
                    ) {
                        IconButton(
                            { addMenu = !addMenu },
                            modifier = Modifier.rotate(animateFloatAsState(if (addMenu) 45f else 0f).value)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            {},
                        ) {
                            Icon(
                                Icons.Default.Check,
                                null,
                            )
                        }
                    }
                }
            }
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedVisibility(
                    addMenu && !isPlaying,
                    enter = slideInHorizontally { it * -1 },
                    exit = slideOutHorizontally(
                        tween(
                            delayMillis = if (isPlaying) 1000 else 0,
                            durationMillis = 700
                        )
                    ) { it * -1 }) {
                    Column(
                        Modifier
                            .width(250.dp)
                            .fillMaxHeight()
                            .padding(10.dp)
                            .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("我的貼圖", fontSize = 25.sp)
                        LazyColumn(
                            Modifier.fillMaxSize()
                        ) {
                            items(canUseImage, key = { it }) {
                                UriImage(
                                    it,
                                    context,
                                    null,
                                    modifier = Modifier
                                        .pointerInput(Unit) {
                                            val img =
                                                Sticker(
                                                    it,
                                                    position,
                                                    mutableStateOf(Offset.Zero),
                                                    mutableStateOf(1f)
                                                )

                                            detectDragGesturesAfterLongPress(onDrag = { change, offset ->
                                                if (positionAndTimeImage.firstOrNull { e -> e.uri == it } == null)
                                                    positionAndTimeImage += img
                                                img.offset.value += offset
                                            }, onDragEnd = { canUseImage.remove(it) })
                                        }
                                )
                            }
                        }
                    }
                }
            }
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
                        { videoView?.seekTo((it * videoDuration).toInt()) },
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

@Composable
fun VideoSeekBar(
    isPlay: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onChange: (Float) -> Unit,
    value: Float,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton({
            if (isPlay)
                onPause()
            else
                onResume()
        }) {
            if (isPlay)
                Icon(painterResource(R.drawable.baseline_pause_24), null)
            else
                Icon(Icons.Default.PlayArrow, null)
        }
        Slider(
            value,
            onChange,
            modifier = Modifier.weight(1f)
        )
    }
}