package com.umc.todait.feature.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.todait.R
import com.umc.todait.ui.component.AuthLogoTopOffset
import com.umc.todait.ui.component.TodaitLogoMark
import com.umc.todait.ui.theme.BgGradientBottom
import com.umc.todait.ui.theme.BgGradientTop
import com.umc.todait.ui.theme.Gray300
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.KakaoBrown
import com.umc.todait.ui.theme.KakaoYellow
import com.umc.todait.ui.theme.Pink800
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 앱 최초 진입 화면. 소셜/이메일 로그인 진입점만 제공한다.
 * 카카오/구글 로그인은 SDK 연동 전까지 onKakaoLoginClick/onGoogleLoginClick을 TODO로 둔다.
 */
@Composable
fun LoginScreen(
    onKakaoLoginClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    onEmailLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BgGradientTop, BgGradientBottom),
                ),
            )
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = AuthLogoTopOffset),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 220.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SocialLoginButton(
                text = stringResource(R.string.login_kakao_button),
                containerColor = KakaoYellow,
                contentColor = KakaoBrown,
                onClick = onKakaoLoginClick,
                icon = { KakaoIcon() },
            )
            SocialLoginButton(
                text = stringResource(R.string.login_google_button),
                containerColor = White,
                contentColor = Gray900,
                borderColor = Gray300,
                onClick = onGoogleLoginClick,
                icon = {
                    Image(
                        painter = painterResource(R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(45.dp),
                    )
                },
            )
            Text(
                text = stringResource(R.string.login_email_button),
                style = MaterialTheme.typography.bodyMedium,
                color = Pink800,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(onClick = onEmailLoginClick),
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    borderColor: Color? = null,
) {
    val shape = CircleShape
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(containerColor)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(Modifier.size(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun KakaoIcon() {
    Image(
        painter = painterResource(R.drawable.ic_kakao),
        contentDescription = null,
        modifier = Modifier.size(20.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    TodaitTheme {
        LoginScreen(
            onKakaoLoginClick = {},
            onGoogleLoginClick = {},
            onEmailLoginClick = {},
        )
    }
}
