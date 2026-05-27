package com.contest.memocut

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun AskScreen(model: MainViewModel, videoUri: Uri) {
    val context = LocalContext.current
    var videoDuration by remember { mutableIntStateOf(1) }
    var videoViewSize by remember { mutableStateOf(IntSize.Zero) }
    var videoView: VideoView? by remember { mutableStateOf(null) }
    var videoViewPos by remember { mutableStateOf(Rect.Zero) }
    var isPlaying by remember { mutableStateOf(false) }
    var addMenu by remember { mutableStateOf(false) }
    var checkDialog by remember { mutableStateOf(false) }
    var placeImage by remember { mutableStateOf<Sticker?>(null) }
    var videoViewScale by remember { mutableStateOf(1f) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    var imageSize by remember {
        mutableStateOf(with(density) {
            IntSize(
                150.dp.toPx().toInt(),
                150.dp.toPx().toInt()
            )
        })
    }
    LaunchedEffect(videoViewSize) {
        delay(200)
        videoView?.seekTo(model.position)
        while (true) {
            model.position = videoView?.currentPosition ?: 0
            delay(100)
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
                            model.stickers
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
            },
        contentAlignment = Alignment.Center
    ) {
        placeImage?.let {
            UriImage(
                it.uri, context, null, modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationX = it.offset.value.x
                        translationY = it.offset.value.y
                    }
                    .zIndex(1f))
        }
        Box(
            Modifier
                .background(Color.White),
        ) {
            for (i in model.stickers) {
                if (model.position in i.time..(i.time + 5000))
                    UriImage(
                        i.uri, context, null, modifier = Modifier
                            .size(with(density) {
                                val maxPx = min(videoViewSize.width, videoViewSize.height)

                                val maxDp = if (maxPx > 0) {
                                    (maxPx / with(density) { 1.dp.toPx() }).dp
                                } else {
                                    Dp.Infinity // 或直接跳過
                                }
                                (150.dp * i.scale.value * videoViewScale).coerceIn(
                                    1.dp,
                                    maxDp
                                )
                            })
                            .graphicsLayer {
                                translationX = i.offset.value.x * videoViewSize.width
                                translationY = i.offset.value.y * videoViewSize.height
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures(true) { centroid, pan, zoom, rotation ->
                                    val x =
                                        (i.offset.value.x + pan.x / videoViewSize.width.toFloat()).coerceIn(
                                            0f,
                                            1f - (imageSize.width * i.scale.value / videoViewSize.width)
                                            // (videoViewSize.width - imageSize.width).toFloat()
                                        )
                                    println(imageSize.height)
                                    println(videoViewSize.height)
                                    println((imageSize.height * i.scale.value / videoViewSize.height))
                                    val y =
                                        (i.offset.value.y + pan.y / videoViewSize.height.toFloat()).coerceIn(
                                            0f,
                                            1f - (imageSize.height * i.scale.value / videoViewSize.height)
                                            //(videoViewSize.height - imageSize.height).toFloat()
                                        )
                                    i.offset.value = Offset(x, y)

                                    val oldScale = i.scale.value
                                    val newScale = (i.scale.value * zoom).coerceIn(
                                        0.5f,
                                        min(
                                            model.videoViewOSize.width,
                                            model.videoViewOSize.height
                                        ) / with(
                                            density
                                        ) { 150.dp.toPx() })

                                    val scaleFactor = newScale / oldScale
                                    val origin = i.offset.value.copy(
                                        x = x * videoViewSize.width,
                                        y = y * videoViewSize.height
                                    ) + centroid - centroid * scaleFactor
                                    i.offset.value = Offset(
                                        origin.x / videoViewSize.width,
                                        origin.y / videoViewSize.height
                                    )
                                    i.scale.value = newScale
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
                        }
                        setOnCompletionListener {
                            isPlaying = false
                        }
                    }
                }, modifier = Modifier
                    .onGloballyPositioned { videoViewPos = it.boundsInWindow() }
                    .onSizeChanged {
                        videoViewSize = it
                        if (model.isO < 2) {
                            model.videoViewOSize = it
                            model.isO++
                        }
                        videoViewScale = it.height.toFloat() / model.videoViewOSize.height.toFloat()
                    }
            )
        }
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
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(top = 25.dp)
                    )
                    Row(
                        Modifier
                            .background(Color.White)
                            .padding(vertical = 5.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                        Text("出題模式", fontSize = 25.sp)
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
                            items(model.canUseImage, key = { it.hashCode() }) {
                                var y by remember { mutableFloatStateOf(0f) }
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
                                                    model.position,
                                                    mutableStateOf(Offset(0f, y)),
                                                    mutableFloatStateOf(1f)
                                                )

                                            detectDragGesturesAfterLongPress(onDrag = { change, offset ->
                                                placeImage = img
                                                img.offset.value += offset
                                            }, onDragEnd = {
//                                                if (model.stickers.firstOrNull { e -> e.uri == it } == null && model.stickers.find { it.time in (position - 1)..(position + 1) } == null) {
                                                if (videoViewPos.contains(img.offset.value) && model.stickers.find { it.time in (model.position - 1)..(model.position + 1) } == null)
                                                    model.stickers += img.copy(
                                                        offset = mutableStateOf(
                                                            Offset(
                                                                (img.offset.value.x - videoViewPos.left) / videoViewSize.width,
                                                                (img.offset.value.y - videoViewPos.top) / videoViewSize.height
                                                            )
                                                        )
                                                    )

                                                if (model.stickers.find { e -> e.uri == it } != null)
                                                    model.canUseImage.remove(it)
                                                img.offset.value = Offset(0f, y)
                                                placeImage = null
                                            })
                                        }
                                )
                            }
                            item {
                                AnimatedVisibility(model.stickers.isNotEmpty()) {
                                    Column(
                                        Modifier.animateItem(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        HorizontalDivider(Modifier.padding(10.dp), thickness = 2.dp)
                                        Text("已使用貼圖", fontSize = 25.sp)
                                    }
                                }
                            }
                            items(model.stickers, key = { "${it.uri}${it.time}" }) {
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
                                    Column() {
                                        IconButton({
                                            model.canUseImage += it.uri
                                            model.stickers.remove(it)
                                        }) {
                                            Icon(
                                                painterResource(R.drawable.outline_delete_24),
                                                null
                                            )
                                        }
                                        TextButton({ videoView?.seekTo(it.time) }) {
                                            Text(
                                                "在${
                                                    (it.time / 60000).toString().padStart(2, '0')
                                                }:${
                                                    (it.time.toFloat() / 1000 % 60).toInt()
                                                        .toString()
                                                        .padStart(2, '0')
                                                }"
                                            )
                                        }
                                    }
                                }
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
                        {
                            videoView?.seekTo((it * videoDuration).toInt())
                        },
                        {
                            videoView?.start()
                            Thread.sleep(200)
                            videoView?.pause()
                        },
                        model.position / videoDuration.toFloat(),
                        modifier = Modifier.fillMaxWidth(.95f)
                    )
                    Text(
                        "${
                            (model.position / 60000).toString().padStart(2, '0')
                        }:${
                            (model.position.toFloat() / 1000 % 60).toInt().toString()
                                .padStart(2, '0')
                        }"
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
    onChangeEnd: () -> Unit = {},
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
            onValueChangeFinished = onChangeEnd,
            modifier = Modifier.weight(1f)
        )
    }
}