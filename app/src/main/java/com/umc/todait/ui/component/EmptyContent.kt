package com.umc.todait.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.R

/**
 * 데이터가 비어 있는 상태 공통 컴포넌트. (뼈대 — 실제 디자인 적용 예정)
 * 조회 결과가 없을 때 안내 문구를 화면 중앙에 노출한다.
 */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmptyContentPreview() {
    EmptyContent(
        title = "저장된 코스가 없어요.",
        description = "코스 생성에서 코스를 생성해 보세요.",
        icon = R.drawable.ic_saved_courses_none
    )
}
@Composable
fun EmptyContent(
    title: String,
    description: String,
    icon: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = Gray500,
                fontSize = 16.sp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = Gray500,
                fontSize = 12.sp,
            )
        }
    }
}
