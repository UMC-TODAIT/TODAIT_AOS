package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 소셜 온보딩(`PATCH /api/members/me/onboarding`) 요청의 약관 동의 항목 1건. `termId` 기반.
 * 일반 회원가입(`POST /api/auth/signup`)은 `termType` 기반의 [SignupTermAgreementDto]를 쓴다 — 두 API가
 * 서로 다른 키를 요구한다(백엔드에 온보딩도 termType 통일 가능한지 확인 중).
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class TermAgreementDto(
    @SerializedName("termId") val termId: Long,
    @SerializedName("agreed") val agreed: Boolean,
)
