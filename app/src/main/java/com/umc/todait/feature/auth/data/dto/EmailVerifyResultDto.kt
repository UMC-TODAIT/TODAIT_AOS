package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 회원가입용 이메일 인증번호 확인 응답 result (POST /api/auth/email/verify-code).
 * emailVerificationToken은 이후 `POST /api/auth/signup` 요청에 그대로 실어야 한다.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class EmailVerifyResultDto(
    @SerializedName("emailVerificationToken") val emailVerificationToken: String,
)
