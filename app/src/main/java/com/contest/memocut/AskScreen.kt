package com.contest.memocut

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.VideoView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.TintableCompoundButton
import kotlinx.coroutines.Runnable
import org.w3c.dom.Text
import kotlin.time.toDuration

@Composable
fun AskScreen(model: MainViewModel, uri: Uri) {
    val context = LocalContext.current
    var videoDuration by remember { mutableIntStateOf(1) }
    var position by remember { mutableIntStateOf(0) }
    var videoView: VideoView? by remember { mutableStateOf(null) }
    var isPlay by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.width(150.dp)) { }
            Column(Modifier.weight(1f)) {
                AndroidView(factory = { context ->
                    VideoView(context).apply {
                        setVideoURI(uri)
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
            Column(Modifier.width(150.dp)) { }
        }
    }
}