package com.umc.todait.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.R
import com.umc.todait.ui.theme.Gray350
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Pink400

/**
 * 네트워크 로딩 상태 공통 컴포넌트. (뼈대 — 실제 디자인 적용 예정)
 * UiState.Loading 일 때 화면 중앙에 노출한다.
 */

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun LoadingIndicatorPreview() {
    LoadingIndicator()
}
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            )
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFAF7),
                        Color(0xFFFFEDED)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(101.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val strokeWidth = 10.dp.toPx()

                    drawCircle(
                        color = Color.White,
                        style = Stroke(
                            width = strokeWidth
                        )
                    )

                    drawArc(
                        color = Pink400,
                        startAngle = rotation,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                }

                Image(
                    painter = painterResource(
                        id = R.drawable.ic_icon_todait
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(
                        width = 36.dp,
                        height = 32.dp
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "로딩중...",
                fontSize = 16.sp,
                color = Gray800
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "잠시만 기다려주세요",
                fontSize = 12.sp,
                color = Gray350
            )
        }
    }
}