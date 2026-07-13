package com.umc.todait.feature.auth.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.auth.data.dto.EmailCodeRequestDto
import com.umc.todait.feature.auth.data.dto.EmailCodeVerifyRequestDto
import com.umc.todait.feature.auth.data.dto.EmailVerifyResultDto
import com.umc.todait.feature.auth.data.dto.LoginRequestDto
import com.umc.todait.feature.auth.data.dto.LoginResultDto
import com.umc.todait.feature.auth.data.dto.NicknameAvailabilityResultDto
import com.umc.todait.feature.auth.data.dto.OnboardingRequestDto
import com.umc.todait.feature.auth.data.dto.OnboardingResultDto
import com.umc.todait.feature.auth.data.dto.PasswordResetRequestDto
import com.umc.todait.feature.auth.data.dto.PasswordResetVerifyResultDto
import com.umc.todait.feature.auth.data.dto.SignupRequestDto
import com.umc.todait.feature.auth.data.dto.SignupResultDto
import com.umc.todait.feature.auth.data.dto.TermAgreementDto
import com.umc.todait.feature.auth.data.dto.TokenRefreshRequestDto
import com.umc.todait.feature.auth.data.dto.TokenRefreshResultDto
import com.umc.todait.feature.auth.data.service.AuthService
import javax.inject.Inject

/**
 * 인증(member/auth) 도메인 데이터 접근 계층.
 * Service 호출을 safeApiCall 로 감싸 ViewModel 에는 ApiResult 만 노출한다.
 */
class AuthRepository @Inject constructor(
    private val authService: AuthService,
) {

    suspend fun login(email: String, password: String): ApiResult<LoginResultDto> =
        safeApiCall { authService.login(LoginRequestDto(email = email, password = password)) }

    suspend fun signup(
        email: String,
        password: String,
        passwordCheck: String,
        nickname: String,
        emailVerificationToken: String,
        termAgreements: List<TermAgreementDto>,
    ): ApiResult<SignupResultDto> = safeApiCall {
        authService.signup(
            SignupRequestDto(
                email = email,
                password = password,
                passwordCheck = passwordCheck,
                nickname = nickname,
                emailVerificationToken = emailVerificationToken,
                termAgreements = termAgreements,
            ),
        )
    }

    suspend fun refreshToken(refreshToken: String): ApiResult<TokenRefreshResultDto> =
        safeApiCall { authService.refreshToken(TokenRefreshRequestDto(refreshToken = refreshToken)) }

    /** 회원가입용 이메일 인증번호 발송. 이미 가입된 이메일이면 AUTH409로 실패한다. */
    suspend fun sendSignupEmailCode(email: String): ApiResult<Unit> =
        safeApiCall { authService.sendSignupEmailCode(EmailCodeRequestDto(email = email)) }

    /** 회원가입용 이메일 인증번호 확인. 성공하면 signup 요청에 실을 emailVerificationToken을 반환한다. */
    suspend fun verifySignupEmailCode(email: String, code: String): ApiResult<EmailVerifyResultDto> =
        safeApiCall { authService.verifySignupEmailCode(EmailCodeVerifyRequestDto(email = email, code = code)) }

    /** 닉네임 중복 확인. 실패(예외)가 아니라 정상 응답의 available 값으로 중복 여부를 판단한다. */
    suspend fun checkNicknameAvailability(nickname: String): ApiResult<NicknameAvailabilityResultDto> =
        safeApiCall { authService.checkNicknameAvailability(nickname) }

    /** 비밀번호 재설정용 이메일 인증번호 발송. 가입 안 된 이메일이면 AUTH404로 실패한다. */
    suspend fun sendPasswordResetEmailCode(email: String): ApiResult<Unit> =
        safeApiCall { authService.sendPasswordResetEmailCode(EmailCodeRequestDto(email = email)) }

    /** 비밀번호 재설정용 이메일 인증번호 확인. 성공하면 다음 단계에 쓸 resetToken을 반환한다. */
    suspend fun verifyPasswordResetEmailCode(email: String, code: String): ApiResult<PasswordResetVerifyResultDto> =
        safeApiCall { authService.verifyPasswordResetEmailCode(EmailCodeVerifyRequestDto(email = email, code = code)) }

    suspend fun resetPassword(
        resetToken: String,
        newPassword: String,
        newPasswordCheck: String,
    ): ApiResult<Unit> = safeApiCall {
        authService.resetPassword(
            PasswordResetRequestDto(
                resetToken = resetToken,
                newPassword = newPassword,
                newPasswordCheck = newPasswordCheck,
            ),
        )
    }

    /** 소셜 간편가입 온보딩 완료. temporaryToken은 카카오/구글 콜백에서 신규 회원일 때 발급받은 값이다. */
    suspend fun completeOnboarding(
        temporaryToken: String,
        nickname: String,
        termAgreements: List<TermAgreementDto>,
    ): ApiResult<OnboardingResultDto> = safeApiCall {
        authService.completeOnboarding(
            bearerTemporaryToken = "Bearer $temporaryToken",
            request = OnboardingRequestDto(nickname = nickname, termAgreements = termAgreements),
        )
    }

    /**
     * 로그아웃. 서버가 refreshToken을 폐기한다.
     * accessToken을 서버가 무효화하지 않으므로, 호출부(ViewModel)가 성공 여부와 무관하게
     * TokenDataStore.clearTokens()로 로컬 토큰을 반드시 지워야 한다.
     */
    suspend fun logout(accessToken: String): ApiResult<Unit> =
        safeApiCall { authService.logout(bearerAccessToken = "Bearer $accessToken") }
}
