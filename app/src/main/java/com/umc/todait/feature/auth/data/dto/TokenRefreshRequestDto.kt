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
 * 토큰 재발급 응답 result.
 *
 * Rotation 적용 후에는 refreshToken도 새 값으로 함께 내려오고, 요청에 쓴 기존 refreshToken은 폐기된다.
 * ⚠️ Rotation 배포 **전** 서버는 accessToken만 내려주므로 [refreshToken]은 nullable이다 —
 * null이면 기존에 저장된 refreshToken을 그대로 유지해야 한다.
 */
data class TokenRefreshResultDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String? = null,
)
