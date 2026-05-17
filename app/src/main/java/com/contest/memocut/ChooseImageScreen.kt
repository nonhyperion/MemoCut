package com.contest.memocut

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ChooseImageScreen(model: MainViewModel, uri: Uri) {
    val context = LocalContext.current
    var uriList by remember { mutableStateOf<List<Uri>>(listOf()) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
            uriList += it
        }
    LaunchedEffect(Unit) {

    }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("選擇貼紙", fontSize = 35.sp)
        if (uriList.isNotEmpty())
            Row(Modifier.height(50.dp)) {
                for (i in uriList) {
                    val byteArray =
                        context.contentResolver.openInputStream(i).use { it?.readBytes() }
                    byteArray?.let {
                        Image(
                            BitmapFactory.decodeByteArray(
                                it, 0, it.size
                            ).asImageBitmap(), null
                        )
                    }
                }
            }
        Row() {
            Button({
                launcher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }) {
                Text("選取貼紙")
            }
            Button({
                model.push { AskScreen(model, uri, uriList) }
            }, enabled = uriList.isNotEmpty()) {
                Text("下一步")
            }
        }
    }
}