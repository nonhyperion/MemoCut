package com.contest.memocut

import android.net.Uri
import android.os.strictmode.UntaggedSocketViolation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultScreen(model: MainViewModel, ansSticker: List<Uri>, playerSticker: List<Uri>) {
    var correctCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 0 until ansSticker.size) {
            if (ansSticker[i] == playerSticker[i])
                correctCount++
        }
    }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("答題結果", fontSize = 50.sp, fontWeight = FontWeight.Bold)
        Text("$correctCount/${ansSticker.size}", fontSize = 30.sp)
        Text("${((correctCount / ansSticker.size.toFloat()) * 100).toInt()}%", fontSize = 30.sp)
    }
}