package com.umc.todait.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
 * 이메일 로그인 화면. 실제 로그인 API 연동 전까지는 버튼 클릭 시 에러 상태만 로컬로 목업한다.
 */
@Composable
fun EmailLoginScreen(
    onSignupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoginError by rememberSaveable { mutableStateOf(false) }

    val isLoginEnabled = email.isNotBlank() && password.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgGradientTop, BgGradientBottom)))
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = AuthLogoTopOffset),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 220.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    isLoginError = false
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.email_login_email_placeholder)) },
                singleLine = true,
                shape = CircleShape,
                colors = emailLoginTextFieldColors(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    isLoginError = false
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.email_login_password_placeholder)) },
                singleLine = true,
                shape = CircleShape,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = stringResource(R.string.signup_password_visibility_toggle_content_description),
                            tint = Gray500,
                        )
                    }
                },
                colors = emailLoginTextFieldColors(),
            )

            if (isLoginError) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.email_login_error_message),
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
                    .background(if (isLoginEnabled) Pink400 else DisabledButtonGray)
                    .clickable(enabled = isLoginEnabled) {
                        // TODO: 실제 로그인 API 연동 전까지의 로컬 목업 — 항상 에러로 표시
                        isLoginError = true
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.email_login_button),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isLoginEnabled) White else Gray500,
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
private fun EmailLoginScreenPreview() {
    TodaitTheme {
        EmailLoginScreen(onSignupClick = {})
    }
}
