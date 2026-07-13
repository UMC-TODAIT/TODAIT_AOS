package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 이메일만 실어 인증번호를 요청하는 공통 요청 형태.
 * 회원가입용 발송(POST /api/auth/email/send-code)과
 * 비밀번호 재설정용 발송(POST /api/auth/password-reset/email/send)이 형태가 같아 공용으로 쓴다.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class EmailCodeRequestDto(
    @SerializedName("email") val email: String,
)

/**
 * 이메일 + 인증번호를 실어 검증하는 공통 요청 형태.
 * 회원가입용 확인(POST /api/auth/email/verify-code)과
 * 비밀번호 재설정용 확인(POST /api/auth/password-reset/email/verify)이 형태가 같아 공용으로 쓴다.
 */
data class EmailCodeVerifyRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String,
)
