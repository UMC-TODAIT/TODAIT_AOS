package com.umc.todait.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.todait.R

/**
 * 인증 화면(로그인/이메일 로그인 등)에서 로고를 화면 상단으로부터 고정 배치할 때 쓰는 offset.
 * 화면마다 하단 버튼/입력칸 높이가 달라도 로고 시작 위치가 항상 같도록, 이 값을 기준으로
 * `Box` + `Alignment.TopCenter`로 배치한다(하단 콘텐츠 높이와 무관하게 고정).
 */
val AuthLogoTopOffset = 160.dp

/**
 * 투데잇 로고 마크(핀/하트 모양). 로그인/이메일 로그인 등 인증 화면에서 공용으로 쓴다.
 * 화면마다 크기가 다르면 로고 위치가 달라 보이므로, 항상 이 컴포저블을 통해 동일한 크기로 사용한다.
 */
@Composable
fun TodaitLogoMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_todait_logo),
        contentDescription = null,
        modifier = modifier.size(width = 139.dp, height = 120.dp),
    )
}
