package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 카카오 네이티브 SDK 로그인 성공 후 받은 accessToken을 백엔드로 전달 (POST /api/auth/kakao/login).
 */
data class KakaoLoginRequestDto(
    @SerializedName("accessToken") val accessToken: String,
)

/**
 * 구글 Credential Manager 로그인 성공 후 받은 idToken을 백엔드로 전달 (POST /api/auth/google/login).
 */
data class GoogleLoginRequestDto(
    @SerializedName("idToken") val idToken: String,
)

/**
 * 카카오/구글 소셜 로그인 응답 result (두 API 공통).
 *
 * ⚠️ TODAIT_BE 스펙(배포 서버 Swagger) 기준. 필드 추가/변경 시 명세와 대조해 수정한다.
 * - 기존 회원: accessToken/refreshToken이 채워지고 onboardingToken은 비어 있음 → 바로 로그인 완료.
 * - 신규 회원: onboardingToken이 채워짐 → 온보딩(닉네임/약관) 필요. 이 경우 accessToken 등은 비어 있을 수 있음.
 * (loginStatus 문자열의 정확한 값은 명세에 없어, 신규/기존 판단은 onboardingToken 유무로 한다.)
 */
data class SocialLoginResultDto(
    @SerializedName("loginStatus") val loginStatus: String?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("onboardingToken") val onboardingToken: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("provider") val provider: String?,
)
