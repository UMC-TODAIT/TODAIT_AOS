package com.umc.todait.feature.home.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 홈 화면 상단 인사말에 쓸 회원 정보 (GET /api/members/me 의 result).
 *
 * 닉네임 전용이던 `GET /api/members/me/nickname` 은 폐지되고 "마이페이지 사용자 정보 조회"로 통합됐다.
 * 홈은 그 응답 중 [nickname] 만 필요하므로 나머지 필드는 선언하지 않는다(Gson 이 무시한다).
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class HomeMemberDto(
    @SerializedName("nickname") val nickname: String,
)
