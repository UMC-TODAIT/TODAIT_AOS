package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 비밀번호 재설정 인증번호 확인 응답 result (POST /api/auth/password-reset/email/verify).
 * resetToken은 짧은 유효시간을 가지며, 이후 `PATCH /api/auth/password-reset` 요청에 실어야 한다.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class PasswordResetVerifyResultDto(
    @SerializedName("resetToken") val resetToken: String,
)
