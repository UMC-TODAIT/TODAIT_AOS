package com.umc.todait.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.todait.R
import com.umc.todait.ui.component.AuthLogoTopOffset
import com.umc.todait.ui.component.TodaitLogoMark
import com.umc.todait.ui.theme.BgGradientBottom
import com.umc.todait.ui.theme.BgGradientTop
import com.umc.todait.ui.theme.DisabledButtonGray
import com.umc.todait.ui.theme.Error
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.LoginHeadingPink
import com.umc.todait.ui.theme.Pink100
import com.umc.todait.ui.theme.Pink400
import com.umc.todait.ui.theme.Pink800
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 이메일 로그인 화면(라우트 진입점). ViewModel의 상태/효과를 구독해 실제 로그인 API와 연동한다.
 * 로그인 성공 시 NavigateToHome 효과를 받아 홈으로 이동한다.
 */
@Composable
fun EmailLoginScreen(
    onSignupClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmailLoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EmailLoginEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    EmailLoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onLoginClick = viewModel::onLoginClick,
        onSignupClick = onSignupClick,
        modifier = modifier,
    )
}

@Composable
private fun EmailLoginContent(
    uiState: EmailLoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgGradientTop, BgGradientBottom)))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(AuthLogoTopOffset))
        TodaitLogoMark()
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.email_login_heading),
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 26.sp,
                letterSpacing = (-0.02).em,
            ),
            color = LoginHeadingPink,
        )

        Spacer(Modifier.height(70.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.email_login_email_placeholder)) },
                singleLine = true,
                shape = CircleShape,
                colors = emailLoginTextFieldColors(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.email_login_password_placeholder)) },
                singleLine = true,
                shape = CircleShape,
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = stringResource(R.string.signup_password_visibility_toggle_content_description),
                            tint = Gray500,
                        )
                    }
                },
                colors = emailLoginTextFieldColors(),
            )

            uiState.error?.let { error ->
                val errorText = when (error) {
                    LoginError.InvalidCredentials -> stringResource(R.string.email_login_error_message)
                    is LoginError.General -> error.message
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Pink100)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(CircleShape)
                    .background(if (uiState.isLoginEnabled) Pink400 else DisabledButtonGray)
                    .clickable(enabled = uiState.isLoginEnabled, onClick = onLoginClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.email_login_button),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (uiState.isLoginEnabled) White else Gray500,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.email_login_signup_link),
                style = MaterialTheme.typography.bodyMedium,
                color = Pink800,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .align(Alignment.Start)
                    .clickable(onClick = onSignupClick)
                    .padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun emailLoginTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
    focusedPlaceholderColor = Gray200,
    unfocusedPlaceholderColor = Gray200,
)

@Preview(showBackground = true)
@Composable
private fun EmailLoginContentPreview() {
    TodaitTheme {
        EmailLoginContent(
            uiState = EmailLoginUiState(email = "todait@umc.com", password = "password"),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onSignupClick = {},
        )
    }
}

@Preview(showBackground = true, name = "인증 실패")
@Composable
private fun EmailLoginContentInvalidCredentialsPreview() {
    TodaitTheme {
        EmailLoginContent(
            uiState = EmailLoginUiState(
                email = "todait@umc.com",
                password = "wrongpassword",
                error = LoginError.InvalidCredentials,
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onSignupClick = {},
        )
    }
}

@Preview(showBackground = true, name = "네트워크 오류")
@Composable
private fun EmailLoginContentNetworkErrorPreview() {
    TodaitTheme {
        EmailLoginContent(
            uiState = EmailLoginUiState(
                email = "todait@umc.com",
                password = "test1234",
                error = LoginError.General("연결 상태를 확인해주세요."),
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onSignupClick = {},
        )
    }
}
