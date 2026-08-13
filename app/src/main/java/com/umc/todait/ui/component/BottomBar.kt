package com.umc.todait.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.navigation.BottomTab
import com.umc.todait.ui.theme.Gray100
import com.umc.todait.ui.theme.Gray400
import com.umc.todait.ui.theme.Pink800

/**
 * 하단 탭바(Figma `컴포넌트_GNB`).
 *
 * 시안은 355x64 / 라운드 40 이고, 선택된 탭에만 Gray-100 알약(84x52, 라운드 31)이 깔린다.
 * 폭은 좌우 여백 20dp 로 잡아 화면 폭에 대응한다(393dp 에서 353dp).
 */
@Composable
fun BottomBar(
    currentRoute: String?,
    onTabClick: (BottomTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(BAR_RADIUS)
                )
                .background(
                    Color.White,
                    RoundedCornerShape(BAR_RADIUS)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->

                val selected =
                    currentRoute == tab.route

                Box(
                    modifier = Modifier
                        .weight(1f)
                        // 알약은 탭 칸을 꽉 채우지 않는다(시안 88.75 칸 안에 84).
                        .padding(horizontal = 2.dp)
                        .height(PILL_HEIGHT)
                        .background(
                            if(selected)
                                Gray100
                            else
                                Color.Transparent,
                            RoundedCornerShape(PILL_RADIUS)
                        )
                        .clickable {
                            onTabClick(tab)
                        },
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ){
                        Icon(
                            painter = painterResource(tab.iconRes),
                            contentDescription = null,
                            tint = if(selected) Pink800 else Gray400,
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = tab.label,
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            color =
                                if(selected)
                                    Pink800
                                else
                                    Gray400
                        )
                    }
                }
            }
        }
    }
}

private val BAR_HEIGHT = 64.dp
private val BAR_RADIUS = 40.dp
private val PILL_HEIGHT = 52.dp
private val PILL_RADIUS = 31.dp
