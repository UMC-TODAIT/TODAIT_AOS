package com.umc.todait.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 데이터가 비어 있는 상태 공통 컴포넌트. (뼈대 — 실제 디자인 적용 예정)
 * 조회 결과가 없을 때 안내 문구를 화면 중앙에 노출한다.
 */
@Composable
fun EmptyContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message, textAlign = TextAlign.Center)
    }
}
