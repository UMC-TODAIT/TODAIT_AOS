package com.umc.todait

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.umc.todait.navigation.TodaitApp
import com.umc.todait.ui.theme.TodaitTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 투데잇은 단일 Activity + Compose Navigation 구조를 사용한다.
 * 화면 추가는 navigation/Screen.kt 와 TodaitNavHost.kt 에서 진행한다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodaitTheme {
                TodaitApp()
            }
        }
    }
}
