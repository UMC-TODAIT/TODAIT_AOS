package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 일반 회원가입 요청 (POST /api/auth/signup).
 *
 * `emailVerificationToken`은 이메일 인증번호 확인(`POST /api/auth/email/verify-code`)에서 발급받은 값이고,
 * `termAgreements`는 약관 동의를 다 받은 시점에 함께 실어야 한다 — 즉 이 API 호출 자체가
 * 약관 동의 화면 완료 시점에 일어나야 한다 (이메일/비밀번호 입력 화면에서 바로 호출하면 안 됨).
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class SignupRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("passwordCheck") val passwordCheck: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("emailVerificationToken") val emailVerificationToken: String,
    @SerializedName("termAgreements") val termAgreements: List<TermAgreementDto>,
)

/**
 * 일반 회원가입 응답 result.
 */
data class SignupResultDto(
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("email") val email: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
)
