package com.contest.memocut

import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType.Companion.Uri
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import java.net.URI

@Composable
fun ChooseVideoScreen(model: MainViewModel) {
    val context = LocalContext.current
    var uri by remember { mutableStateOf<Uri?>(null) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            uri = it
        }
    LaunchedEffect(Unit) {

    }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("選擇影片", fontSize = 35.sp)
        if (uri != null)
            AndroidView(factory = { context ->
                VideoView(context).apply {
                    setVideoURI(uri)
                    setOnPreparedListener {
                        seekTo(duration / 2)
                    }
                }
            }, modifier = Modifier.heightIn(0.dp, 200.dp))
        Row() {
            Button({
                launcher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.SingleMimeType(
                            "video/mp4"
                        )
                    )
                )
            }) {
                Text("選擇影片")
            }
            Button({
                model.push { ChooseImageScreen(model, uri!!) }
            }, enabled = uri != null) {
                Text("下一步")
            }
        }
    }
}