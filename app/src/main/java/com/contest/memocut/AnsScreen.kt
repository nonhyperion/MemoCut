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
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.key
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import kotlinx.coroutines.Runnable

@Composable
fun AnsScreen(model: MainViewModel, videoUri: Uri, ansStickers: List<Sticker>) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val list = remember { ansStickers.map { it.uri }.toMutableStateList().apply { shuffle() } }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("請由先到後排出出現順序", fontSize = 30.sp)
        LazyRow {
            items(list, key = { it }) {
                var offset by remember { mutableStateOf(Offset.Zero) }
                var zIndex by remember { mutableFloatStateOf(0f) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .animateItem()
                        .graphicsLayer({
                            translationX = offset.x
                        })
                        .padding(10.dp)
                        .border(
                            1.dp,
                            Color.Black,
                            CardDefaults.shape
                        )
                        .zIndex(zIndex)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painterResource(R.drawable.outline_equal_24),
                            null,
                            modifier = Modifier.pointerInput(Unit) {
                                detectDragGestures(onDragEnd = {
                                    with(density) {
                                        val oldIndex = list.indexOf(it)
                                        val newIndex =
                                            (oldIndex + (offset.x / 150.dp.toPx()).toInt()).coerceIn(
                                                0,
                                                list.size - 1
                                            )
                                        println(newIndex)
                                        list.remove(it)
                                        list.add(newIndex, it)
                                    }

                                    offset =
                                        Offset.Zero
                                    zIndex = 0f
                                }) { change, dragAmount ->
                                    zIndex = 1f
                                    offset += dragAmount
                                }
                            })
                        UriImage(it, context, null, modifier = Modifier.size(150.dp))
                    }
                }
            }
        }
        Button({
            model.push {
                ResultScreen(
                    model,
                    ansStickers.sortedBy { it.time }.map { it.uri },
                    list
                )
            }
        }) { Text("送出答案") }
    }
}