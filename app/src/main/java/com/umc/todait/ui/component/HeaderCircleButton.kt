package com.umc.todait.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray200

/** 시안의 헤더 원형 버튼: Cream 원 + Gray-200 테두리 (Figma node 1678:9035, 코스 저장 화면 등에서 공통으로 쓴다). */
@Composable
fun HeaderCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Cream)
            .border(1.dp, Gray200, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
