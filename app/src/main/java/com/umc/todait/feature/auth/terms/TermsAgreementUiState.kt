package com.umc.todait.feature.auth.terms

import com.umc.todait.feature.auth.data.dto.TermAgreementDto

/**
 * 약관 동의 화면 진입 플로우. 동의 완료 후 이동할 다음 화면(회원가입 폼 / 소셜 닉네임 설정)을 구분한다.
 */
enum class TermsFlow(val route: String) {
    EMAIL("email"),
    KAKAO("kakao"),
    GOOGLE("google"),
    ;

    companion object {
        /** 네비게이션 인자(문자열)로부터 플로우를 복원한다. 알 수 없으면 EMAIL로 취급. */
        fun fromRoute(value: String?): TermsFlow =
            entries.firstOrNull { it.route.equals(value, ignoreCase = true) } ?: EMAIL
    }
}

/**
 * 약관 목록 한 항목.
 *
 * `termType`은 회원가입·온보딩 API가 요구하는 값(SERVICE/PRIVACY/LOCATION/MARKETING),
 * `termId`는 화면 내부 식별/약관 상세 이동용. `GET /api/terms`가 폐기(노션 이동)돼 두 값 모두 앱에 고정한다.
 */
data class TermItemUiModel(
    val termId: Long,
    val termType: String,
    val title: String,
    val isRequired: Boolean,
    val isAgreed: Boolean,
    // 필수 약관만 상세 화면(화살표)을 노출한다. 선택 약관은 상세 없음.
    val hasDetail: Boolean = false,
)

/**
 * 약관 동의 화면 상태.
 * terms는 GET /api/terms 스펙 확정 전까지 더미 목록으로 채워진다(ViewModel 참고).
 */
data class TermsAgreementUiState(
    val flow: TermsFlow = TermsFlow.EMAIL,
    val terms: List<TermItemUiModel> = emptyList(),
) {
    val isAllAgreed: Boolean
        get() = terms.isNotEmpty() && terms.all { it.isAgreed }

    /** 필수 약관을 모두 동의해야 [다음] 버튼이 활성화된다(선택 약관은 무관). */
    val isNextEnabled: Boolean
        get() = terms.any { it.isRequired } && terms.filter { it.isRequired }.all { it.isAgreed }
}

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface TermsAgreementEffect {
    /**
     * 약관 동의 완료 → 다음 화면(이메일 회원가입 / 소셜 닉네임 설정)으로 이동.
     *
     * agreedTerms는 signup/onboarding API 요청에 실릴 termAgreements 값이다. 다음 화면들이
     * 아직 실제 API를 호출하지 않는 로컬 목업 상태라 지금은 쓰이지 않는다 — 그 화면들에
     * 진짜 API 연동을 붙일 때 이 값을 nav 인자 등으로 넘기도록 같이 정리한다.
     */
    data class NavigateNext(
        val flow: TermsFlow,
        val agreedTerms: List<TermAgreementDto>,
    ) : TermsAgreementEffect

    /** 필수 약관 항목의 화살표 탭 → 약관 상세 화면으로 이동. */
    data class NavigateToDetail(val termId: Long) : TermsAgreementEffect
}
