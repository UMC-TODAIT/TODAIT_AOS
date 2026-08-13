package com.umc.todait.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.ui.theme.Green700
import com.umc.todait.ui.theme.Pink900
import com.umc.todait.ui.theme.White

/**
 * 취향 설정(분위기/음식 선택) 카드 한 장. 두 화면이 완전히 동일한 카드 스펙을 쓰므로 공용 컴포넌트로 뺐다.
 *
 * Figma: 카드 169x200dp, 세로 그라데이션(위 [gradientStart] → 아래 [gradientEnd]).
 * 선택 시 카드 테두리 Green-700(#819158) 2dp, 우상단 원이 흰 배경 → Pink-900(#ED9896) 채움으로 바뀐다.
 */
@Composable
fun TasteSelectionCard(
    title: String,
    hashtags: String,
    gradientStart: Color,
    gradientEnd: Color,
    selectedGradientStart: Color,
    selectedGradientEnd: Color,
    @DrawableRes decorationRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 선택하면 같은 계열에서 채도가 올라간 그라데이션으로 바뀐다(Figma).
    val background = if (isSelected) {
        listOf(selectedGradientStart, selectedGradientEnd)
    } else {
        listOf(gradientStart, gradientEnd)
    }
    Box(
        modifier = modifier
            // 시안은 169dp 고정이지만 그리드 칸을 채우게 두어 화면 폭에 대응한다(393dp에서 168.5dp).
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .clip(RoundedCornerShape(CORNER_RADIUS))
            .background(Brush.verticalGradient(background))
            .then(
                if (isSelected) {
                    Modifier.border(SELECTED_BORDER_WIDTH, Green700, RoundedCornerShape(CORNER_RADIUS))
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
    ) {
        // Figma: 72x72, 카드 우하단에서 오른쪽 15.94 / 아래 15.51 만큼 띄운 자리(169x200 기준 left 81.06 / top 112.49).
        // 카드 폭이 화면에 따라 달라져도 같은 자리에 붙도록 좌상단 절대좌표 대신 우하단 기준으로 잡는다.
        // 문양마다 원본 비율이 달라(예: '조용한'/'한식'은 가로로 납작함) ContentScale.Fit이 세로로
        // 남는 여백을 위아래로 나눠 채우면 문양마다 밑바닥 위치가 들쭉날쭉해진다.
        // alignment를 BottomCenter로 고정해 모든 문양이 72x72 박스 하단에 맞춰지도록 한다.
        Image(
            painter = painterResource(decorationRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = DECORATION_END, bottom = DECORATION_BOTTOM)
                .size(DECORATION_SIZE),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = TEXT_TOP),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TEXT_GAP),
        ) {
            // lineHeight 를 지정하지 않으면 Compose 기본 행간이 시안(SUIT 기본 ≈1.25em)보다 넓어 두 줄 간격이 벌어진다.
            Text(
                text = title,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = White,
                textAlign = TextAlign.Center,
            )
            Text(
                text = hashtags,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = White,
                textAlign = TextAlign.Center,
            )
        }
        SelectionIndicator(
            isSelected = isSelected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 13.dp, end = 13.6.dp),
        )
    }
}

/** 우상단 선택 표시 원. 미선택 = 흰 배경, 선택 = 1dp 흰 테두리 + Pink-900 채움. */
@Composable
private fun SelectionIndicator(isSelected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(INDICATOR_SIZE)
            .clip(CircleShape)
            .background(White)
            .then(
                if (isSelected) {
                    Modifier
                        .padding(1.dp)
                        .clip(CircleShape)
                        .background(Pink900)
                } else {
                    Modifier
                },
            ),
    )
}

private val CARD_HEIGHT = 200.dp
private val CORNER_RADIUS = 12.dp
private val SELECTED_BORDER_WIDTH = 2.dp
private val INDICATOR_SIZE = 10.4.dp
private val DECORATION_SIZE = 72.dp
private val DECORATION_END = 15.94.dp
private val DECORATION_BOTTOM = 15.51.dp
private val TEXT_TOP = 12.dp
private val TEXT_GAP = 6.dp
