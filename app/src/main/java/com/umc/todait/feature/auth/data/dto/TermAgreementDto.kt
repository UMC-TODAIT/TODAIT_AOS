package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 약관 동의 항목 1건. 일반 회원가입(`POST /api/auth/signup`)과
 * 소셜 온보딩(`PATCH /api/members/me/onboarding`) 요청에 공통으로 쓰인다.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class TermAgreementDto(
    @SerializedName("termId") val termId: Long,
    @SerializedName("agreed") val agreed: Boolean,
)
