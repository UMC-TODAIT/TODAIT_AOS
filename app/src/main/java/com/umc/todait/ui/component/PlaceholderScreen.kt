package com.umc.todait.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 화면 구현 전 임시 플레이스홀더.
 * 담당자는 feature 패키지에 실제 Screen을 만들고 NavHost에서 교체한다.
 */
@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$name 화면 (구현 예정)",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
