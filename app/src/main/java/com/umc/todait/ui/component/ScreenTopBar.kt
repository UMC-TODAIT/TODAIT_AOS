package com.umc.todait.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.DividerLine
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.TodaitTheme

/**
 * 화면 공통 헤더. Figma 개편 시안(예: node 534:13524 기준장소설정_검색)의 배치를 그대로 따른다.
 *
 * - 배경은 Cream. 상태바 영역의 Pink-100 띠는 전역 [TopBar] 가 그리므로 여기서 다시 칠하지 않는다.
 *   (헤더까지 Pink-100 으로 칠하면 두 띠가 이어져 헤더가 두 배로 길어 보인다)
 * - 좌우 24dp / 상하 11dp 안에 뒤로가기(‹) · 타이틀(20sp Medium, Gray-800) · 확인(✓)을 배치.
 * - 그 아래 Gray-100 구분선 1dp, 좌우 20dp 인셋(본문 좌우 여백과 동일).
 *
 * [onConfirm] 이 null 이면 우측 ✓ 를 그리지 않는다(뒤로가기만 있는 화면).
 */
@Composable
fun ScreenTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backContentDescription: String = "뒤로가기",
    onConfirm: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    confirmContentDescription: String = "확인",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Cream),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HORIZONTAL_PADDING, vertical = VERTICAL_PADDING),
        ) {
            HeaderBackButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = onBack,
                contentDescription = backContentDescription,
            )
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                color = Gray800,
            )
            if (onConfirm != null) {
                HeaderCheckButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    contentDescription = confirmContentDescription,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DIVIDER_INSET)
                .height(1.dp)
                .background(DividerLine),
        )
    }
}

private val HORIZONTAL_PADDING = 24.dp
private val VERTICAL_PADDING = 11.dp
private val DIVIDER_INSET = 20.dp

@Preview(showBackground = true, backgroundColor = 0xFFFDFBF3)
@Composable
private fun ScreenTopBarPreview() {
    TodaitTheme {
        Column {
            ScreenTopBar(title = "기준 장소 설정", onBack = {}, onConfirm = {})
            ScreenTopBar(title = "코스 구성하기", onBack = {}, onConfirm = {}, confirmEnabled = false)
            ScreenTopBar(title = "이용약관", onBack = {})
        }
    }
}
