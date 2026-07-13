package com.umc.todait.feature.auth.data.service

import com.umc.todait.core.network.BaseResponse
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
import com.umc.todait.feature.auth.data.dto.TokenRefreshRequestDto
import com.umc.todait.feature.auth.data.dto.TokenRefreshResultDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 인증(member/auth) 도메인 Retrofit 서비스.
 *
 * 카카오/구글 로그인(GET /api/auth/kakao, /google + 콜백)은 브라우저 리다이렉트 기반 OAuth 흐름으로
 * 설계돼 있어 일반 JSON Retrofit 호출과 맞지 않는다 — 네이티브 SDK 연동 방식이 확정된 뒤 별도로 추가한다.
 * (자세한 내용은 memory의 reference_auth_api_schemas 참고)
 *
 * ⚠️ 모든 엔드포인트는 TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
interface AuthService {

    /** 이메일 로그인 — POST /api/auth/login */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): BaseResponse<LoginResultDto>

    /** 일반 회원가입 — POST /api/auth/signup (약관 동의까지 다 받은 시점에 호출) */
    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequestDto): BaseResponse<SignupResultDto>

    /** 토큰 재발급 — POST /api/auth/token/refresh */
    @POST("api/auth/token/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequestDto): BaseResponse<TokenRefreshResultDto>

    /** 회원가입용 이메일 인증번호 발송 — POST /api/auth/email/send-code (이미 가입된 이메일이면 AUTH409) */
    @POST("api/auth/email/send-code")
    suspend fun sendSignupEmailCode(@Body request: EmailCodeRequestDto): BaseResponse<Unit>

    /** 회원가입용 이메일 인증번호 확인 — POST /api/auth/email/verify-code */
    @POST("api/auth/email/verify-code")
    suspend fun verifySignupEmailCode(@Body request: EmailCodeVerifyRequestDto): BaseResponse<EmailVerifyResultDto>

    /** 닉네임 중복 확인 — GET /api/members/nickname-availability?nickname= */
    @GET("api/members/nickname-availability")
    suspend fun checkNicknameAvailability(@Query("nickname") nickname: String): BaseResponse<NicknameAvailabilityResultDto>

    /** 비밀번호 재설정용 이메일 인증번호 발송 — POST /api/auth/password-reset/email/send (미가입 이메일이면 AUTH404) */
    @POST("api/auth/password-reset/email/send")
    suspend fun sendPasswordResetEmailCode(@Body request: EmailCodeRequestDto): BaseResponse<Unit>

    /** 비밀번호 재설정용 이메일 인증번호 확인 — POST /api/auth/password-reset/email/verify */
    @POST("api/auth/password-reset/email/verify")
    suspend fun verifyPasswordResetEmailCode(
        @Body request: EmailCodeVerifyRequestDto,
    ): BaseResponse<PasswordResetVerifyResultDto>

    /** 새 비밀번호 설정 — PATCH /api/auth/password-reset */
    @PATCH("api/auth/password-reset")
    suspend fun resetPassword(@Body request: PasswordResetRequestDto): BaseResponse<Unit>

    /**
     * 소셜 간편가입 온보딩 완료 — PATCH /api/members/me/onboarding
     * temporaryToken은 카카오/구글 콜백에서 신규 회원으로 판단됐을 때 발급받은 값이다.
     */
    @PATCH("api/members/me/onboarding")
    suspend fun completeOnboarding(
        @Header("Authorization") bearerTemporaryToken: String,
        @Body request: OnboardingRequestDto,
    ): BaseResponse<OnboardingResultDto>

    /**
     * 로그아웃 — POST /api/auth/logout. 서버가 refreshToken을 폐기한다.
     * accessToken 자체는 서버가 무효화하지 않으므로, 호출부가 로컬 토큰을 반드시 지워야 한다.
     */
    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") bearerAccessToken: String): BaseResponse<Unit>
}
