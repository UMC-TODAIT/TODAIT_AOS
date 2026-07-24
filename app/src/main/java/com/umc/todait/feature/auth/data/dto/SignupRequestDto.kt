package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 일반 회원가입 요청 (POST /api/auth/signup).
 *
 * `termAgreements`는 약관 동의를 다 받은 시점에 함께 실어야 한다 — 즉 이 API 호출 자체가
 * 약관 동의 화면 완료 시점에 일어나야 한다 (이메일/비밀번호 입력 화면에서 바로 호출하면 안 됨).
 * 이메일 인증은 별도 토큰 없이, 서버가 인증번호 확인 시 저장해둔 "인증완료" 상태를 이메일 기준으로 확인한다.
 * 비밀번호 확인(passwordCheck)은 앱에서만 검증하고 서버에는 실제 비밀번호만 전달한다.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class SignupRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("termAgreements") val termAgreements: List<SignupTermAgreementDto>,
)

/**
 * 일반 회원가입의 약관 동의 항목 1건. 온보딩(`PATCH /api/members/me/onboarding`)의 `termId` 기반
 * [TermAgreementDto]와 달리, 이 API는 `termType`(SERVICE/PRIVACY/LOCATION/MARKETING) 문자열을 쓴다.
 */
data class SignupTermAgreementDto(
    @SerializedName("termType") val termType: String,
    @SerializedName("agreed") val agreed: Boolean,
)

/**
 * 일반 회원가입 응답 result. HTTP 상태 코드는 201 Created.
 */
data class SignupResultDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
)
