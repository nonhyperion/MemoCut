package com.contest.memocut

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(model: MainViewModel) {
    val context = LocalContext.current
    Scaffold() { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(.75f))
            Text("MemoCut", fontSize = 50.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Button({ model.push { ChooseVideoScreen(model) } }) { Text("開始遊戲") }
            Button({}) { Text("玩法說明") }
            Button({ (context as Activity).finish() }) { Text("結束遊戲") }
            Spacer(Modifier.weight(1f))
        }
    }
}