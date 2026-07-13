package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 닉네임 중복 확인 응답 result (GET /api/members/nickname-availability?nickname=).
 *
 * 다른 auth API와 달리, 닉네임이 이미 사용 중이어도 에러가 아니라 이 정상 응답으로
 * `available = false`가 내려온다 — 호출부는 예외 처리가 아니라 이 값으로 화면을 분기해야 한다.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class NicknameAvailabilityResultDto(
    @SerializedName("nickname") val nickname: String,
    @SerializedName("available") val available: Boolean,
)
