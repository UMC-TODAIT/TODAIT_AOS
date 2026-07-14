package com.umc.todait.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.umc.todait.R
import androidx.compose.runtime.*
import com.umc.todait.ui.component.CommonDialog
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.ProfileCardBackground
import com.umc.todait.ui.theme.TermsText

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyPageScreenPreview() {
    MyPageScreen(
        navController = rememberNavController()
    )
}
@Composable
fun MyPageScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 20.dp)
    ) {
        var showLogoutDialog by remember {
            mutableStateOf(false)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "마이페이지",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Gray800
        )

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(R.drawable.divider_line),
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
            color = Gray800
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(
            onNoticeClick = {
                navController.navigate("notice")
            },
            onLogoutClick = {
                showLogoutDialog = true
            }
        )

        if (showLogoutDialog) {
            CommonDialog(
                title = "로그아웃",
                message = "로그아웃하시겠습니까?",
                confirmText = "확인",
                onConfirm = {
                    showLogoutDialog = false

                    // TODO 로그아웃 API 호출
                    // TODO 로그인 화면 이동
                },
                onDismiss = {
                    showLogoutDialog = false
                }
            )
        }
    }
}

@Composable
fun ProfileCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProfileCardBackground
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
                    painter = painterResource(R.drawable.ic_my_page_profile),
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
                    color = Gray800
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "todait@naver.com",
                    color = Gray800,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    onNoticeClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
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
                        painter = painterResource(R.drawable.ic_my_page_notice),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                },
                title = "공지사항 및 고객센터",
                onClick = onNoticeClick
            )

            Image(
                painter = painterResource(R.drawable.divider_my_page),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .fillMaxWidth()
                    .height(1.dp)
            )

            SettingItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_my_page_logout),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                },
                title = "로그아웃",
                onClick = onLogoutClick
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        icon()

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = if (title == "로그아웃") {
                TermsText
            } else {
                Gray800
            }
        )

        if (title != "로그아웃") {
            Icon(
                painter = painterResource(R.drawable.ic_my_page_arrow),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}

