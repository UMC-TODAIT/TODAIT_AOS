package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 새 비밀번호 설정 요청 (PATCH /api/auth/password-reset).
 * resetToken은 비밀번호 재설정 인증번호 확인 API에서 발급받은 임시 토큰이다.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class PasswordResetRequestDto(
    @SerializedName("resetToken") val resetToken: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("newPasswordCheck") val newPasswordCheck: String,
)
