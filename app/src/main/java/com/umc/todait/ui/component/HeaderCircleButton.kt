package com.umc.todait.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.todait.R
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.TodaitTheme

/**
 * 시안의 헤더 원형 버튼: Cream 원 + Gray-200 테두리 (Figma node 1678:9035, 코스 저장 화면 등에서 공통으로 쓴다).
 *
 * [enabled] 가 false 면 흐리게 표시하고 클릭도 받지 않는다.
 * 뒤로가기·확인처럼 아이콘이 정해진 버튼은 [HeaderBackButton] / [HeaderCheckButton] 을 쓰고,
 * 그 외 아이콘(닫기 등)은 이 컴포넌트에 content 로 넘긴다.
 *
 * 사진 위에 얹는 뒤로가기는 대비 때문에 반투명 검정 원을 쓰므로 이 컴포넌트를 쓰지 않는다.
 */
@Composable
fun HeaderCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .size(BUTTON_SIZE)
            .clip(CircleShape)
            .background(Cream)
            .border(1.dp, Gray200, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** 헤더 좌측 뒤로가기(‹). */
@Composable
fun HeaderBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "뒤로가기",
) {
    HeaderCircleButton(onClick = onClick, modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.ic_common_chevron_left),
            contentDescription = contentDescription,
            modifier = Modifier.height(ICON_SIZE),
        )
    }
}

/**
 * 헤더 우측 확인(✓).
 * [enabled] 가 false 면 흐리게 표시하고 클릭도 받지 않는다(예: 담은 장소가 없을 때).
 */
@Composable
fun HeaderCheckButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String = "확인",
) {
    HeaderCircleButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Image(
            painter = painterResource(R.drawable.ic_common_check),
            contentDescription = contentDescription,
            modifier = Modifier.width(ICON_SIZE),
        )
    }
}

private val BUTTON_SIZE = 40.dp
private val ICON_SIZE = 16.dp
private const val DISABLED_ALPHA = 0.4f

@Preview(showBackground = true, backgroundColor = 0xFFFDFBF3)
@Composable
private fun HeaderButtonsPreview() {
    TodaitTheme {
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            HeaderBackButton(onClick = {}, modifier = Modifier.align(Alignment.CenterStart))
            HeaderCheckButton(onClick = {}, modifier = Modifier.align(Alignment.Center))
            HeaderCheckButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterEnd),
                enabled = false,
            )
        }
    }
}
