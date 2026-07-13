package com.umc.todait.feature.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.R
import com.umc.todait.ui.theme.DisabledButtonGray
import com.umc.todait.ui.theme.DisabledConfirmGray
import com.umc.todait.ui.theme.Error
import com.umc.todait.ui.theme.Gray300
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray700
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.Pink400
import com.umc.todait.ui.theme.Primary
import com.umc.todait.ui.theme.SignupBackground
import com.umc.todait.ui.theme.Success
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.VerifyPink
import com.umc.todait.ui.theme.White
import kotlinx.coroutines.delay

private const val CODE_TIMEOUT_SECONDS = 180

private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")
private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

private sealed interface EmailVerificationState {
    data object Idle : EmailVerificationState
    data class CodeSent(val secondsLeft: Int) : EmailVerificationState
    data object Verified : EmailVerificationState
    data object Expired : EmailVerificationState
}

/**
 * 회원가입 화면 (이메일 가입 폼). 이메일 인증/비밀번호 검증은 실제 API 연동 전까지 로컬 목업으로 동작한다.
 */
@Composable
fun SignupScreen(
    onBackClick: () -> Unit,
    onSignupComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirm by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var passwordConfirmVisible by rememberSaveable { mutableStateOf(false) }
    var verificationState by remember { mutableStateOf<EmailVerificationState>(EmailVerificationState.Idle) }

    // TODO: 실제 인증번호 발송/확인 API 연동 전까지의 로컬 카운트다운 목업
    LaunchedEffect(verificationState) {
        val current = verificationState
        if (current is EmailVerificationState.CodeSent) {
            if (current.secondsLeft <= 0) {
                verificationState = EmailVerificationState.Expired
            } else {
                delay(1000)
                verificationState = current.copy(secondsLeft = current.secondsLeft - 1)
            }
        }
    }

    val isEmailValid = email.isNotEmpty() && EMAIL_REGEX.matches(email)
    val isPasswordValid = password.isNotEmpty() && PASSWORD_REGEX.matches(password)
    val isPasswordConfirmMatching = passwordConfirm.isNotEmpty() && passwordConfirm == password
    val isSignupEnabled = name.isNotBlank() &&
        verificationState is EmailVerificationState.Verified &&
        isPasswordValid &&
        isPasswordConfirmMatching

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SignupBackground),
    ) {
        SignupTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.signup_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = Gray900,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.signup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
            )
            Spacer(Modifier.height(24.dp))

            AuthTextField(
                label = stringResource(R.string.signup_name_label),
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.signup_name_placeholder),
            )
            Spacer(Modifier.height(20.dp))

            AuthTextField(
                label = stringResource(R.string.signup_email_label),
                value = email,
                onValueChange = { email = it },
                placeholder = stringResource(R.string.signup_email_placeholder),
                enabled = verificationState is EmailVerificationState.Idle,
                keyboardType = KeyboardType.Email,
                trailingIcon = {
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        InlinePillButton(
                            text = if (verificationState is EmailVerificationState.Idle) {
                                stringResource(R.string.signup_send_code_button)
                            } else {
                                stringResource(R.string.signup_resend_code_button)
                            },
                            enabled = isEmailValid,
                            onClick = {
                                // TODO: 이메일 인증번호 발송 API 호출
                                code = ""
                                verificationState = EmailVerificationState.CodeSent(CODE_TIMEOUT_SECONDS)
                            },
                        )
                    }
                },
            )
            if (email.isNotEmpty() && !isEmailValid) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.signup_email_invalid_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = Error,
                )
            }

            if (verificationState !is EmailVerificationState.Idle) {
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.signup_code_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray900,
                    )
                    IconButton(
                        onClick = {
                            // TODO: 이메일 인증번호 재발송 API 호출
                            code = ""
                            verificationState = EmailVerificationState.CodeSent(CODE_TIMEOUT_SECONDS)
                        },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.signup_code_refresh_content_description),
                            tint = Gray500,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                val isVerified = verificationState is EmailVerificationState.Verified
                val secondsLeft = (verificationState as? EmailVerificationState.CodeSent)?.secondsLeft ?: 0
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = verificationState is EmailVerificationState.CodeSent,
                    placeholder = { Text(stringResource(R.string.signup_code_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = authTextFieldColors(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end=12.dp)) {
                            if (verificationState is EmailVerificationState.CodeSent) {
                                Text(
                                    text = "%d:%02d".format(secondsLeft / 60, secondsLeft % 60),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Primary,
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            InlinePillButton(
                                text = stringResource(R.string.signup_confirm_code_button),
                                enabled = verificationState is EmailVerificationState.CodeSent && code.isNotBlank(),
                                onClick = {
                                    // TODO: 이메일 인증번호 확인 API 호출
                                    verificationState = EmailVerificationState.Verified
                                },
                            )
                        }
                    },
                )
                if (isVerified || verificationState is EmailVerificationState.Expired) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isVerified) {
                            stringResource(R.string.signup_code_verified_message)
                        } else {
                            stringResource(R.string.signup_code_expired_message)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isVerified) Success else Error,
                    )
                }
            }

            if (verificationState is EmailVerificationState.Verified) {
                Spacer(Modifier.height(20.dp))
                AuthTextField(
                    label = stringResource(R.string.signup_password_label),
                    value = password,
                    onValueChange = { password = it },
                    placeholder = stringResource(R.string.signup_password_placeholder),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardType = KeyboardType.Password,
                    trailingIcon = {
                        PasswordVisibilityToggle(
                            visible = passwordVisible,
                            onToggle = { passwordVisible = !passwordVisible },
                        )
                    },
                )
                if (password.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isPasswordValid) {
                            stringResource(R.string.signup_password_valid_message)
                        } else {
                            stringResource(R.string.signup_password_invalid_message)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPasswordValid) Success else Error,
                    )
                }

                Spacer(Modifier.height(20.dp))
                AuthTextField(
                    label = stringResource(R.string.signup_password_confirm_label),
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    placeholder = stringResource(R.string.signup_password_confirm_placeholder),
                    visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardType = KeyboardType.Password,
                    trailingIcon = {
                        PasswordVisibilityToggle(
                            visible = passwordConfirmVisible,
                            onToggle = { passwordConfirmVisible = !passwordConfirmVisible },
                        )
                    },
                )
                if (passwordConfirm.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isPasswordConfirmMatching) {
                            stringResource(R.string.signup_password_match_message)
                        } else {
                            stringResource(R.string.signup_password_mismatch_message)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPasswordConfirmMatching) Success else Error,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .height(56.dp)
                .clip(CircleShape)
                .background(if (isSignupEnabled) Pink400 else DisabledButtonGray)
                .clickable(enabled = isSignupEnabled, onClick = onSignupComplete),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.signup_complete_button),
                style = MaterialTheme.typography.labelLarge,
                fontSize = 18.sp,
                color = if (isSignupEnabled) White else Gray500,
            )
        }
    }
}

@Composable
private fun SignupTopBar(onBackClick: () -> Unit) {
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
                    contentDescription = stringResource(R.string.signup_back_content_description),
                    tint = Color.Unspecified,
                )
            }
            Text(
                text = stringResource(R.string.signup_title),
                style = MaterialTheme.typography.titleMedium,
                color = Gray900,
            )
        }
        HorizontalDivider(color = Gray300)
    }
}

@Composable
private fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Gray900,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = { Text(placeholder) },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = authTextFieldColors(),
        )
    }
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gray300,
    unfocusedBorderColor = Gray300,
    disabledBorderColor = Gray300,
    disabledTextColor = Gray700,
)

@Composable
private fun InlinePillButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) VerifyPink else DisabledConfirmGray)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = White)
    }
}

@Composable
private fun PasswordVisibilityToggle(visible: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
            contentDescription = stringResource(R.string.signup_password_visibility_toggle_content_description),
            tint = Gray500,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignupScreenPreview() {
    TodaitTheme {
        SignupScreen(onBackClick = {}, onSignupComplete = {})
    }
}
