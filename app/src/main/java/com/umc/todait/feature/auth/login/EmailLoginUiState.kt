package com.umc.todait.feature.auth.login

/**
 * 이메일 로그인 화면 상태.
 */
data class EmailLoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: LoginError? = null,
) {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

/**
 * 로그인 실패 종류. 서버 인증 실패와 네트워크/일시적 오류를 구분해 다른 문구를 보여준다.
 */
sealed interface LoginError {
    /**
     * 서버 인증 실패(잘못된 이메일/비밀번호).
     * 보안상 이메일이 틀렸는지 비밀번호가 틀렸는지는 구분하지 않고 고정 문구를 보여준다.
     */
    data object InvalidCredentials : LoginError

    /** 네트워크/일시적 오류. 공통 에러 정책(toUiError)의 문구를 그대로 사용한다. */
    data class General(val message: String) : LoginError
}

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface EmailLoginEffect {
    data object NavigateToHome : EmailLoginEffect
}
