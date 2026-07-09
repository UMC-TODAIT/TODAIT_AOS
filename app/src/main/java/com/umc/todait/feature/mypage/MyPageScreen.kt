package com.umc.todait.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.R

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF3))
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "마이페이지",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color(0xFF222222)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(R.drawable.ic_line_74),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileCard()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "앱 설정 및 계정",
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = Color(0xFF222222)
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard()
    }
}

@Composable
fun ProfileCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEB)
        )
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.ic_group_1171275966),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "투데잇",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF222222)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "todait@naver.com",
                    color = Color(0xFF222222),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SettingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column {
            SettingItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_group_1171275965),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                },
                title = "알림 설정"
            )

            Image(
                painter = painterResource(R.drawable.ic_line_78),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .fillMaxWidth()
                    .height(1.dp)
            )

            SettingItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_component_9),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                },
                title = "공지사항 및 고객센터"
            )

            Image(
                painter = painterResource(R.drawable.ic_line_78),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .fillMaxWidth()
                    .height(1.dp)
            )

            SettingItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_component_10),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                },
                title = "로그아웃"
            )
        }
    }
}


@Composable
fun SettingItem(
    icon: @Composable () -> Unit,
    title: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        icon()

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = if (title == "로그아웃") {
                Color(0xFF888888)
            } else {
                Color(0xFF222222)
            }
        )

        if (title != "로그아웃") {
            Icon(
                painter = painterResource(R.drawable.ic_polygon_17),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}