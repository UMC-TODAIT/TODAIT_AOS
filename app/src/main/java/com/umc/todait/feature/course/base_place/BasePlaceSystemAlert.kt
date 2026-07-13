package com.umc.todait.feature.course.base_place

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.R
import com.umc.todait.ui.theme.Error
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray50
import com.umc.todait.ui.theme.Gray600
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Pink100
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 기준 장소 설정 시스템 알럿(와이어프레임: 시스템알럿1/2).
 *
 * 딤 배경(Gray-200 50%) 위에 Gray-600 다이얼로그(334×200)를 띄운다.
 * - [title]: SemiBold 16, 흰색, 중앙
 * - [description]: SemiBold 12, Gray-200, 중앙
 * - [취소](Gray-50) / [확인](Pink-100) 버튼
 *
 * 두 알럿(선택 요청/확정 확인)이 같은 레이아웃을 쓰므로 문구·콜백만 달리해 재사용한다.
 * [errorMessage] 는 확정 검증 실패(지원 지역 외 등) 시에만 노출한다(Figma 기본형엔 없음).
 */
@Composable
fun BasePlaceSystemAlert(
    title: String,
    description: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    errorMessage: String? = null,
) {
    // 딤 배경. 바깥 영역 탭 시 닫는다(리플 없이).
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray200.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCancel,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(334.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Gray600)
                // 다이얼로그 내부 탭이 딤 배경으로 전파돼 닫히지 않도록 소비한다.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray200,
                textAlign = TextAlign.Center,
            )
            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Error,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                AlertButton(
                    text = stringResource(R.string.base_place_confirm_negative),
                    background = Gray50,
                    onClick = onCancel,
                )
                AlertButton(
                    text = stringResource(R.string.base_place_confirm_positive),
                    background = Pink100,
                    onClick = onConfirm,
                )
            }
            Spacer(Modifier.height(44.dp))
        }
    }
}

/** 알럿 하단 버튼(120×40, 라운드 10). 라벨은 Gray-800(#222). */
@Composable
private fun AlertButton(
    text: String,
    background: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Gray800,
        )
    }
}

@Preview(name = "시스템알럿1(선택 요청)", showBackground = true)
@Composable
private fun BasePlaceSystemAlertSelectRequiredPreview() {
    TodaitTheme {
        BasePlaceSystemAlert(
            title = stringResource(R.string.base_place_select_required_title),
            description = stringResource(R.string.base_place_select_required_desc),
            onConfirm = {},
            onCancel = {},
        )
    }
}

@Preview(name = "시스템알럿2(확정 확인)", showBackground = true)
@Composable
private fun BasePlaceSystemAlertConfirmPreview() {
    TodaitTheme {
        BasePlaceSystemAlert(
            title = stringResource(R.string.base_place_confirm_title, "뀌노이"),
            description = stringResource(R.string.base_place_confirm_desc, "뀌노이"),
            onConfirm = {},
            onCancel = {},
        )
    }
}
