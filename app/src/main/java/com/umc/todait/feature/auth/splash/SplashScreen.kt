package com.umc.todait.feature.auth.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.todait.R
import com.umc.todait.ui.component.TodaitLogoMark
import com.umc.todait.ui.theme.BgGradientBottom
import com.umc.todait.ui.theme.BgGradientTop
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.TodaitTheme

/**
 * 앱 최초 진입 화면(라우트 진입점). 저장된 토큰을 확인하는 동안 로고만 보여주다가,
 * 로그인 상태면 홈으로, 아니면 로그인 화면으로 자동 이동한다(버튼 없음).
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SplashEffect.NavigateToHome -> onNavigateToHome()
                SplashEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    SplashContent(modifier = modifier)
}

@Composable
private fun SplashContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgGradientTop, BgGradientBottom))),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(SPLASH_TOP_SPACER_WEIGHT))
        TodaitLogoMark()
        Spacer(Modifier.height(20.dp))
        Image(
            painter = painterResource(R.drawable.ic_todait_wordmark),
            contentDescription = stringResource(R.string.login_logo_content_description),
            modifier = Modifier.height(32.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.login_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = Gray900,
        )
        Spacer(Modifier.weight(SPLASH_BOTTOM_SPACER_WEIGHT))
    }
}

/** 로고 위쪽 여백 비율(값이 클수록 로고가 아래로 내려간다). */
private const val SPLASH_TOP_SPACER_WEIGHT = 1f

/** 로고 아래쪽 여백 비율(값이 클수록 로고가 위로 올라간다). */
private const val SPLASH_BOTTOM_SPACER_WEIGHT = 1.6f

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    TodaitTheme {
        SplashContent()
    }
}
