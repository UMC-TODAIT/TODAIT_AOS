package com.umc.todait.feature.auth.login

/**
 * 이메일 로그인 화면 상태.
 */
data class EmailLoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginError: Boolean = false,
) {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface EmailLoginEffect {
    data object NavigateToHome : EmailLoginEffect
}
