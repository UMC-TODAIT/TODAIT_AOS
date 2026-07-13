package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 토큰 재발급 요청 (POST /api/auth/token/refresh).
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class TokenRefreshRequestDto(
    @SerializedName("refreshToken") val refreshToken: String,
)

/**
 * 토큰 재발급 응답 result. refreshToken은 회전하지 않으므로 accessToken만 내려온다.
 */
data class TokenRefreshResultDto(
    @SerializedName("accessToken") val accessToken: String,
)
