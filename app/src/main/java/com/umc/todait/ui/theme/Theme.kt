package com.umc.todait.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    onBackground = Gray900,
    onSurface = Gray900,
    onSurfaceVariant = Gray500,
    outline = Gray300,
    error = Error,
)

/**
 * 앱 전역 테마. MVP는 라이트 모드만 지원한다.
 */
@Composable
fun TodaitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = TodaitTypography,
    ) {
        // style 을 지정하지 않은 Text 도 전역 폰트(SUIT)를 상속하도록 기본 TextStyle 에 폰트를 주입한다.
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = Suit),
            content = content,
        )
    }
}
