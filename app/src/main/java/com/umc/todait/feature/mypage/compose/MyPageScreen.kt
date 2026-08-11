package com.umc.todait.feature.mypage.compose

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
import androidx.compose.ui.res.stringResource
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.todait.navigation.Screen
import com.umc.todait.ui.theme.Gray200

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
    modifier: Modifier = Modifier,
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isLogoutCompleted) {
        if (uiState.isLogoutCompleted) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    when {
        uiState.isLoading -> {
            LoadingIndicator()
        }

        uiState.error != null -> {
            ErrorContent(
                error = uiState.error!!,
                onRetry = {
                    viewModel.getMyPage()
                },
                onDismiss = {
                    viewModel.clearError()
                }
            )
        }

        else -> {
            var showLogoutDialog by remember {
                mutableStateOf(false)
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Cream)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(11.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "마이페이지",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray800
                    )
                }

                Spacer(modifier = Modifier.height(11.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = Gray200
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileCard(
                    nickname = uiState.nickname,
                    email = uiState.email,
                    profileImageUrl = uiState.profileImageUrl,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "앱 설정 및 계정",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Gray800
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsCard(
                    onNoticeClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://tranquil-paw-d58.notion.site/39dd2aae5cbb80d6a7f7ea326777ab10".toUri()
                        )
                        context.startActivity(intent)
                    },
                    onCustomerCenterClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://tranquil-paw-d58.notion.site/3b4d2aae5cbb80b2a29ce870dc70da14".toUri()
                        )
                        context.startActivity(intent)
                    },
                    onLogoutClick = {
                        showLogoutDialog = true
                    }
                )

                if (showLogoutDialog) {
                    CommonDialog(
                        title = "로그아웃하시겠습니까?",
                        onConfirm = {
                            showLogoutDialog = false
                            viewModel.logout()
                        },
                        onDismiss = {
                            showLogoutDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    nickname: String,
    email: String?,
    profileImageUrl: String? = null,
) {
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
                if (profileImageUrl.isNullOrBlank()) {
                    Image(
                        painter = painterResource(R.drawable.ic_my_page_profile),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = nickname,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Gray800
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email ?: stringResource(R.string.mypage_email_not_provided),
                    color = Gray800,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    onNoticeClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onCustomerCenterClick: () -> Unit,
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
                    title = "공지사항",
                    onClick = onNoticeClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    thickness = 1.dp,
                    color = Gray200
                )

                SettingItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_mypage_center),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    },
                    title = "고객센터",
                    onClick = onCustomerCenterClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    thickness = 1.dp,
                    color = Gray200
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