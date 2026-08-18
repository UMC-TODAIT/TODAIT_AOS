package com.umc.todait.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.umc.todait.ui.theme.Pink100

/**
 * 전역 상단 Pink-100 띠. 상태바 영역을 이 색으로 덮는 역할도 겸한다.
 *
 * MainActivity 가 edge-to-edge 라 이 띠는 상태바 **뒤에** 깔린다. 44dp 로 고정하면
 * 상태바가 그보다 높은 기기(펀치홀·노치 등 실기기 다수)에서 띠가 상태바를 다 덮지 못하고,
 * 바로 아래 화면 헤더가 상태바에 물린다. 그래서 시안 높이(44dp)와 실제 상태바 높이 중 큰 값을 쓴다.
 */
@Composable
fun TopBar(
    modifier: Modifier = Modifier
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(maxOf(BAND_HEIGHT, statusBarHeight))
            .background(Pink100)
    )
}

/** Figma 시안 기준 띠 높이. */
private val BAND_HEIGHT: Dp = 44.dp
