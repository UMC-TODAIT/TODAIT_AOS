package com.umc.todait.feature.auth.signup

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")
private val NICKNAME_REGEX = Regex("^[0-9A-Za-z가-힣]{2,12}$")

/** 이메일 인증번호 발송/확인 진행 상태. */
sealed interface EmailVerificationState {
    data object Idle : EmailVerificationState
    data class CodeSent(val secondsLeft: Int) : EmailVerificationState

    /** [emailVerificationToken]은 회원가입 요청(POST /api/auth/signup)에 그대로 실어야 한다. */
    data class Verified(val emailVerificationToken: String) : EmailVerificationState
    data object Expired : EmailVerificationState
}

/**
 * 회원가입 화면 상태.
 *
 * [nickname]은 화면 라벨은 "이름"이지만 서버 계약상 nickname 필드로 전송된다(명세에 별도 이름 필드 없음).
 */
data class SignupUiState(
    val nickname: String = "",
    val email: String = "",
    val code: String = "",
    val verificationState: EmailVerificationState = EmailVerificationState.Idle,
    val password: String = "",
    val passwordConfirm: String = "",
    val isSendingCode: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val isSubmitting: Boolean = false,
    val sendCodeError: String? = null,
    val verifyCodeError: String? = null,
    val submitError: String? = null,
) {
    val isNicknameValid: Boolean
        get() = NICKNAME_REGEX.matches(nickname)

    val isEmailValid: Boolean
        get() = email.isNotEmpty() && EMAIL_REGEX.matches(email)

    val isPasswordValid: Boolean
        get() = password.isNotEmpty() && PASSWORD_REGEX.matches(password)

    val isPasswordConfirmMatching: Boolean
        get() = passwordConfirm.isNotEmpty() && passwordConfirm == password

    val canSendCode: Boolean
        get() = isEmailValid && !isSendingCode

    val canVerifyCode: Boolean
        get() = verificationState is EmailVerificationState.CodeSent && code.isNotBlank() && !isVerifyingCode

    val isSignupEnabled: Boolean
        get() = isNicknameValid &&
            verificationState is EmailVerificationState.Verified &&
            isPasswordValid &&
            isPasswordConfirmMatching &&
            !isSubmitting
}

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface SignupEffect {
    /** 회원가입 완료 후 홈으로 이동. */
    data object NavigateToComplete : SignupEffect
}
