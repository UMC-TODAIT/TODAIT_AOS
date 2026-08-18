package com.umc.todait.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

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
 * 시스템 글꼴 크기 배율 상한.
 *
 * 기기 설정(설정 > 디스플레이 > 글꼴 크기)을 크게 잡아둔 실기기에서는 fontScale 이 1.3~2.0 까지 올라가,
 * 시안대로 고정 높이를 쓴 칩·배지·헤더에서 글자가 잘린다. 화면 레이아웃은 [androidx.compose.foundation.layout.heightIn]
 * 으로 늘어나게 고쳤지만, 시연 중 임의의 기기에서 시안이 무너지지 않도록 배율 자체에도 상한을 둔다.
 * (1.0 미만은 그대로 존중 — 작게 쓰는 사용자는 잘릴 일이 없다.)
 */
private const val MAX_FONT_SCALE = 1.15f

/**
 * 앱 전역 테마. MVP는 라이트 모드만 지원한다.
 */
@Composable
fun TodaitTheme(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val cappedDensity = remember(density) {
        if (density.fontScale <= MAX_FONT_SCALE) density
        else Density(density = density.density, fontScale = MAX_FONT_SCALE)
    }
    CompositionLocalProvider(LocalDensity provides cappedDensity) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = TodaitTypography,
        ) {
            // style 을 지정하지 않은 Text 도 전역 폰트(SUIT)를 상속하도록 기본 TextStyle 에 폰트를 주입한다.
            // (style = TextStyle(...) 을 직접 넘기면 이 값이 통째로 대체되므로, 그런 곳은 fontFamily = Suit 를 직접 적는다.)
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = Suit),
                content = content,
            )
        }
    }
}
