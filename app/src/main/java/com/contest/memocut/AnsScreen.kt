package com.contest.memocut

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import kotlinx.coroutines.Runnable

@Composable
fun AnsScreen(model: MainViewModel, videoUri: Uri, imageUri: List<Uri>) {
    val canUseImage = remember { imageUri.toMutableStateList() }
    val context = LocalContext.current
    var videoDuration by remember { mutableIntStateOf(1) }
    var position by remember { mutableIntStateOf(0) }
    var videoView: VideoView? by remember { mutableStateOf(null) }
    val stickers = remember { mutableStateListOf<Sticker>() }
    var isPlaying by remember { mutableStateOf(false) }
    var addMenu by remember { mutableStateOf(false) }
    var delArea by remember { mutableStateOf(false) }
    var checkDialog by remember { mutableStateOf(false) }
    var change by remember { mutableStateOf(0) }
    var changeSticker by remember {
        mutableStateOf(
            Sticker(
                "".toUri(),
                0,
                mutableStateOf(Offset.Zero),
                mutableStateOf(0f)
            )
        )
    }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    LaunchedEffect(change) {
        if (change > 1) {
            with(density) {
                if (changeSticker.offset.value.x > configuration.screenWidthDp.dp.toPx() - 100.dp.toPx()) {
                    canUseImage += changeSticker.uri
                    stickers.remove(changeSticker)
                }
            }
            delArea = false
            change = 0
        }
    }

    if (checkDialog)
        AlertDialog(
            { checkDialog = false },
            {
                TextButton({
                    model.push {
                        LookScreen(
                            model,
                            videoUri,
                            stickers
                        )
                    }
                }) { Text("下一步") }
            },
            dismissButton = { TextButton({ checkDialog = false }) { Text("返回") } },
            text = { Text("出題完成?") })

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(indication = null, interactionSource = null) {
                videoView?.pause()
                isPlaying = false
            }
    ) {
        for (i in stickers) {
            if (position in i.time..(i.time + 5000))
                UriImage(
                    i.uri, context, null, modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer {
                            translationX = i.offset.value.x
                            translationY = i.offset.value.y
                            scaleX = i.scale.value
                            scaleY = i.scale.value
                        }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                if (!isPlaying) {
                                    changeSticker = i
                                    do {
                                        delArea = true
                                        if (change == 0)
                                            change++
                                        val event = awaitPointerEvent()
                                        i.scale.value *= event.calculateZoom()
                                        i.offset.value += event.calculatePan() * i.scale.value
                                    } while (event.changes.any { it.pressed })
                                    change++
                                }
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
                            modifier = Modifier.rotate(animateFloatAsState(if (addMenu) -45f else 0f).value)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            {
                                checkDialog = true
                            },
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
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(canUseImage, key = { it.hashCode() }) {
                                var y by remember { mutableStateOf(0f) }
                                UriImage(
                                    it,
                                    context,
                                    null,
                                    modifier = Modifier
                                        .animateItem()
                                        .onGloballyPositioned { pos ->
                                            y = pos.positionInWindow().y
                                        }
                                        .size(150.dp)
                                        .pointerInput(Unit) {
                                            val img =
                                                Sticker(
                                                    it,
                                                    position,
                                                    mutableStateOf(Offset(0f, y)),
                                                    mutableStateOf(1f)
                                                )

                                            detectDragGesturesAfterLongPress(onDrag = { change, offset ->
                                                if (stickers.firstOrNull { e -> e.uri == it } == null) {
                                                    stickers += img
                                                }
                                                img.offset.value += offset
                                            }, onDragEnd = { canUseImage.remove(it) })
                                        }
                                )
                            }
                            item {
                                AnimatedVisibility(stickers.isNotEmpty()) {
                                    Column(
                                        Modifier.animateItem(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        HorizontalDivider(Modifier.padding(10.dp), thickness = 2.dp)
                                        Text("已使用貼圖", fontSize = 25.sp)
                                    }
                                }
                            }
                            items(stickers, key = { "${it.uri}${it.time}" }) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UriImage(
                                        it.uri,
                                        context,
                                        null,
                                        modifier = Modifier
                                            .size(100.dp)
                                    )
                                    TextButton({ videoView?.seekTo(it.time) }) {
                                        Text(
                                            "在${
                                                (it.time / 60000).toString().padStart(2, '0')
                                            }:${
                                                (it.time.toFloat() / 1000 % 60).toInt().toString()
                                                    .padStart(2, '0')
                                            }"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                AnimatedVisibility(delArea) {
                    Card(
                        Modifier
                            .fillMaxHeight()
                            .width(30.dp), colors = CardDefaults.cardColors(
                            containerColor = Color(
                                0xFFFA4D4D
                            )
                        )
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Delete, null, tint = Color.White)
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