package com.umc.todait.feature.auth.signupcomplete

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.R
import com.umc.todait.ui.theme.BgGradientBottom
import com.umc.todait.ui.theme.BgGradientTop
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.Pink600
import com.umc.todait.ui.theme.Primary
import com.umc.todait.ui.theme.TodaitTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 가입 완료 연출을 보여준 뒤 홈으로 자동 이동하기까지 대기하는 시간. 문양 등장(1.4초)+회전(2초) 연출이 다 끝난 뒤에도 잠깐 멈춰 있도록 여유를 둔다. */
private const val AUTO_NAVIGATE_DELAY_MS = 4100L

/** 로고 등장 스프링 강성. 기본 StiffnessLow보다 낮춰 바운스가 눈에 보이게 천천히 움직인다. */
private const val LOGO_SPRING_STIFFNESS = 90f

/** 문양이 제자리에서 회전하는 바퀴 수(0.7바퀴). */
private const val DECORATION_ROTATION_TURNS = 0.7f

/**
 * 회원가입 완료 화면. 이메일 가입 / 소셜 간편가입(카카오·구글) 두 플로우 모두 여기를 거쳐
 * 일정 시간 뒤 자동으로 홈으로 이동한다.
 */
@Composable
fun SignupCompleteScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(AUTO_NAVIGATE_DELAY_MS)
        onNavigateToHome()
    }

    val brand = stringResource(R.string.signup_complete_brand)
    val message = stringResource(R.string.signup_complete_message)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgGradientTop, BgGradientBottom))),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(120.dp))
        Text(
            text = stringResource(R.string.signup_complete_welcome),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = Gray900,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.Bold)) {
                    append(brand)
                }
                append(message)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Gray900,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(40.dp))
        WelcomeIllustration()
        Spacer(Modifier.weight(1f))

        DotsIndicator()
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.signup_complete_navigating),
            style = MaterialTheme.typography.bodySmall,
            color = Gray500,
        )
        Spacer(Modifier.height(64.dp))
    }
}

/** 로고 뒤에 숨어있던 장식 문양 6개가 각자의 자리로 튀어나오며 회전하는 연출. */
private data class DecorationSpec(
    @DrawableRes val res: Int,
    val size: Dp,
    val offsetX: Dp,
    val offsetY: Dp,
)

private val DECORATIONS = listOf(
    DecorationSpec(R.drawable.ic_course_dialog_4, 46.dp, offsetX = 126.dp, offsetY = 97.dp),
    DecorationSpec(R.drawable.ic_mood_hip, 30.dp, offsetX = (-66).dp, offsetY = (-129).dp),
    DecorationSpec(R.drawable.ic_course_dialog_1, 50.dp, offsetX = 116.dp, offsetY = (-52).dp),
    DecorationSpec(R.drawable.ic_signup_complete_deco_sparkle, 60.dp, offsetX = (-118).dp, offsetY = 50.dp),
    DecorationSpec(R.drawable.ic_course_dialog_3, 55.dp, offsetX = 53.dp, offsetY = 82.dp),
    DecorationSpec(R.drawable.ic_mood_romantic, 49.dp, offsetX = (-105).dp, offsetY = (-85).dp),
)

@Composable
private fun WelcomeIllustration(modifier: Modifier = Modifier) {
    val logoScale = remember { Animatable(0f) }
    // 1단계: 문양이 로고 뒤에서 제자리로 천천히 나온다(위치/크기/투명도). 2단계: 다 나온 뒤 제자리에서 시계방향 회전.
    val positionProgress = remember { Animatable(0f) }
    val rotationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = LOGO_SPRING_STIFFNESS,
                ),
            )
        }
        positionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        )
        rotationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing),
        )
    }
    val density = LocalDensity.current

    Box(modifier = modifier.size(320.dp), contentAlignment = Alignment.Center) {
        DECORATIONS.forEach { deco ->
            Image(
                painter = painterResource(deco.res),
                contentDescription = null,
                modifier = Modifier
                    .size(deco.size)
                    .graphicsLayer {
                        val p = positionProgress.value
                        translationX = with(density) { (deco.offsetX * p).toPx() }
                        translationY = with(density) { (deco.offsetY * p).toPx() }
                        scaleX = p
                        scaleY = p
                        alpha = p.coerceIn(0f, 1f)
                        rotationZ = rotationProgress.value * DECORATION_ROTATION_TURNS * 360f
                    },
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_todait_logo),
            contentDescription = null,
            modifier = Modifier
                .size(width = 139.dp, height = 120.dp)
                .graphicsLayer {
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                },
        )
    }
}

/** 정적 인디케이터(장식용) — 상태에 따라 바뀌지 않는다. Pink-600을 점점 옅게 그라데이션처럼 표현한다. */
@Composable
private fun DotsIndicator(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DOT_ALPHAS.forEach { dotAlpha ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Pink600.copy(alpha = dotAlpha)),
            )
        }
    }
}

private val DOT_ALPHAS = listOf(1f, 0.6f, 0.35f)

@Preview(showBackground = true)
@Composable
private fun SignupCompleteScreenPreview() {
    TodaitTheme {
        SignupCompleteScreen(onNavigateToHome = {})
    }
}
