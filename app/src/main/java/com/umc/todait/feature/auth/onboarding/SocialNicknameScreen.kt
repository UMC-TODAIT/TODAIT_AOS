package com.umc.todait.feature.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.todait.R
import com.umc.todait.ui.theme.DisabledButtonGray
import com.umc.todait.ui.theme.DisabledConfirmGray
import com.umc.todait.ui.theme.Error
import com.umc.todait.ui.theme.GoogleBadgeBlue
import com.umc.todait.ui.theme.GoogleBadgeText
import com.umc.todait.ui.theme.Gray300
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray700
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.KakaoBrown
import com.umc.todait.ui.theme.KakaoYellow
import com.umc.todait.ui.theme.Pink400
import com.umc.todait.ui.theme.SignupBackground
import com.umc.todait.ui.theme.Success
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.VerifyPink
import com.umc.todait.ui.theme.White

/**
 * 소셜 간편가입 닉네임 설정 화면(라우트 진입점). ViewModel의 상태/효과를 구독한다.
 * 닉네임 확정(시작하기) 시 NavigateToComplete 효과를 받아 회원가입 완료 화면으로 이동한다.
 * (약관 동의는 이 화면에 오기 전 TermsAgreementScreen에서 이미 끝낸 상태)
 */
@Composable
fun SocialNicknameScreen(
    onBackClick: () -> Unit,
    onNavigateToComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SocialNicknameViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SocialNicknameEffect.NavigateToComplete -> onNavigateToComplete()
            }
        }
    }

    SocialNicknameContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onNicknameChange = viewModel::onNicknameChange,
        onCheckDuplicate = viewModel::onCheckDuplicate,
        onStartClick = viewModel::onStartClick,
        modifier = modifier,
    )
}

@Composable
private fun SocialNicknameContent(
    uiState: SocialNicknameUiState,
    onBackClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onCheckDuplicate: () -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SignupBackground),
    ) {
        SocialNicknameTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(VerifyPink),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = stringResource(R.string.social_nickname_profile_content_description),
                    tint = White,
                    modifier = Modifier.size(52.dp),
                )
            }

            Spacer(Modifier.height(16.dp))
            ProviderBadge(provider = uiState.provider)

            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.social_nickname_heading),
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Gray900,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.social_nickname_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Gray500,
            )

            Spacer(Modifier.height(28.dp))
            NicknameField(
                value = uiState.nickname,
                onValueChange = onNicknameChange,
                onCheckDuplicate = onCheckDuplicate,
                checkEnabled = uiState.isCheckEnabled,
            )

            if (uiState.status != NicknameStatus.IDLE) {
                Spacer(Modifier.height(10.dp))
                val isAvailable = uiState.status == NicknameStatus.AVAILABLE
                Text(
                    text = stringResource(
                        if (isAvailable) R.string.social_nickname_available_message
                        else R.string.social_nickname_unavailable_message,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = if (isAvailable) Success else Error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(14.dp))
            NicknameRules()
            Spacer(Modifier.height(32.dp))
        }

        if (uiState.submitError != null) {
            Text(
                text = uiState.submitError,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .height(56.dp)
                .clip(CircleShape)
                .background(if (uiState.isStartEnabled) Pink400 else DisabledButtonGray)
                .clickable(enabled = uiState.isStartEnabled, onClick = onStartClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(
                    if (uiState.isSubmitting) R.string.social_nickname_submitting_button
                    else R.string.social_nickname_start_button,
                ),
                style = MaterialTheme.typography.labelLarge,
                fontSize = 18.sp,
                color = if (uiState.isStartEnabled) White else Gray500,
            )
        }
    }
}

@Composable
private fun SocialNicknameTopBar(onBackClick: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_button),
                    contentDescription = stringResource(R.string.social_nickname_back_content_description),
                    tint = Color.Unspecified,
                )
            }
            Text(
                text = stringResource(R.string.social_nickname_title),
                style = MaterialTheme.typography.titleMedium,
                color = Gray900,
            )
        }
        HorizontalDivider(color = Gray300)
    }
}

@Composable
private fun ProviderBadge(provider: SignupProvider) {
    val backgroundColor = when (provider) {
        SignupProvider.KAKAO -> KakaoYellow
        SignupProvider.GOOGLE -> GoogleBadgeBlue
    }
    val textColor = when (provider) {
        SignupProvider.KAKAO -> KakaoBrown
        SignupProvider.GOOGLE -> GoogleBadgeText
    }
    val label = when (provider) {
        SignupProvider.KAKAO -> stringResource(R.string.social_nickname_badge_kakao)
        SignupProvider.GOOGLE -> stringResource(R.string.social_nickname_badge_google)
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}

@Composable
private fun NicknameField(
    value: String,
    onValueChange: (String) -> Unit,
    onCheckDuplicate: () -> Unit,
    checkEnabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.social_nickname_input_placeholder)) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gray300,
            unfocusedBorderColor = Gray300,
            disabledBorderColor = Gray300,
            disabledTextColor = Gray700,
        ),
        trailingIcon = {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                NicknameCheckButton(enabled = checkEnabled, onClick = onCheckDuplicate)
            }
        },
    )
}

@Composable
private fun NicknameCheckButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) VerifyPink else DisabledConfirmGray)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.social_nickname_check_button),
            style = MaterialTheme.typography.labelLarge,
            color = White,
        )
    }
}

@Composable
private fun NicknameRules(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        NicknameRuleItem(stringResource(R.string.social_nickname_rule_length))
        NicknameRuleItem(stringResource(R.string.social_nickname_rule_special))
        NicknameRuleItem(stringResource(R.string.social_nickname_rule_changeable))
    }
}

@Composable
private fun NicknameRuleItem(text: String) {
    Row {
        Text(text = "•", style = MaterialTheme.typography.bodySmall, color = Gray500)
        Spacer(Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Gray500)
    }
}

@Preview(showBackground = true, name = "카카오 · 기본")
@Composable
private fun SocialNicknameKakaoDefaultPreview() {
    TodaitTheme {
        SocialNicknameContent(
            uiState = SocialNicknameUiState(provider = SignupProvider.KAKAO),
            onBackClick = {}, onNicknameChange = {}, onCheckDuplicate = {}, onStartClick = {},
        )
    }
}

@Preview(showBackground = true, name = "카카오 · 가능")
@Composable
private fun SocialNicknameKakaoAvailablePreview() {
    TodaitTheme {
        SocialNicknameContent(
            uiState = SocialNicknameUiState(
                provider = SignupProvider.KAKAO,
                nickname = "투데잇",
                status = NicknameStatus.AVAILABLE,
            ),
            onBackClick = {}, onNicknameChange = {}, onCheckDuplicate = {}, onStartClick = {},
        )
    }
}

@Preview(showBackground = true, name = "카카오 · 불가능")
@Composable
private fun SocialNicknameKakaoUnavailablePreview() {
    TodaitTheme {
        SocialNicknameContent(
            uiState = SocialNicknameUiState(
                provider = SignupProvider.KAKAO,
                nickname = "투데잇@@",
                status = NicknameStatus.UNAVAILABLE,
            ),
            onBackClick = {}, onNicknameChange = {}, onCheckDuplicate = {}, onStartClick = {},
        )
    }
}

@Preview(showBackground = true, name = "구글 · 기본")
@Composable
private fun SocialNicknameGoogleDefaultPreview() {
    TodaitTheme {
        SocialNicknameContent(
            uiState = SocialNicknameUiState(provider = SignupProvider.GOOGLE),
            onBackClick = {}, onNicknameChange = {}, onCheckDuplicate = {}, onStartClick = {},
        )
    }
}

@Preview(showBackground = true, name = "구글 · 가능")
@Composable
private fun SocialNicknameGoogleAvailablePreview() {
    TodaitTheme {
        SocialNicknameContent(
            uiState = SocialNicknameUiState(
                provider = SignupProvider.GOOGLE,
                nickname = "투데잇",
                status = NicknameStatus.AVAILABLE,
            ),
            onBackClick = {}, onNicknameChange = {}, onCheckDuplicate = {}, onStartClick = {},
        )
    }
}

@Preview(showBackground = true, name = "구글 · 불가능")
@Composable
private fun SocialNicknameGoogleUnavailablePreview() {
    TodaitTheme {
        SocialNicknameContent(
            uiState = SocialNicknameUiState(
                provider = SignupProvider.GOOGLE,
                nickname = "투데잇@@",
                status = NicknameStatus.UNAVAILABLE,
            ),
            onBackClick = {}, onNicknameChange = {}, onCheckDuplicate = {}, onStartClick = {},
        )
    }
}
