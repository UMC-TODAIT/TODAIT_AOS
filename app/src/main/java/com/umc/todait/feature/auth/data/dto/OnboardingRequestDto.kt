package com.umc.todait.feature.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 소셜 간편가입 온보딩 완료 요청 (PATCH /api/members/me/onboarding).
 * 카카오/구글 콜백에서 신규 회원으로 판단됐을 때 발급받은 temporaryToken을
 * `Authorization: Bearer {temporaryToken}` 헤더로 실어 호출한다 (본문에는 없음).
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class OnboardingRequestDto(
    @SerializedName("nickname") val nickname: String,
    @SerializedName("termAgreements") val termAgreements: List<TermAgreementDto>,
)

/**
 * 온보딩 완료 응답 result. 이 시점부터 정식 accessToken/refreshToken으로 로그인 상태가 된다.
 */
data class OnboardingResultDto(
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
)
