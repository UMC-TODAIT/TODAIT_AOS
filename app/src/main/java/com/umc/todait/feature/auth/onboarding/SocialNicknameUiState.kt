package com.umc.todait.feature.auth.onboarding

/**
 * 소셜 간편가입 제공자. 화면 상단 뱃지(카카오=노랑 / 구글=파랑)를 구분한다.
 */
enum class SignupProvider(val route: String) {
    KAKAO("kakao"),
    GOOGLE("google"),
    ;

    companion object {
        /** 네비게이션 인자(문자열)로부터 제공자를 복원한다. 알 수 없으면 KAKAO로 취급. */
        fun fromRoute(value: String?): SignupProvider =
            entries.firstOrNull { it.route.equals(value, ignoreCase = true) } ?: KAKAO
    }
}

/**
 * 닉네임 검사 결과 상태.
 * - IDLE: 아직 확인 전(메시지 없음)
 * - AVAILABLE: 사용 가능(초록 메시지 + 시작하기 활성)
 * - UNAVAILABLE: 형식 위반 또는 중복(빨강 메시지)
 */
enum class NicknameStatus { IDLE, AVAILABLE, UNAVAILABLE }

/**
 * 소셜 간편가입 닉네임 설정 화면 상태.
 */
data class SocialNicknameUiState(
    val provider: SignupProvider = SignupProvider.KAKAO,
    val nickname: String = "",
    val status: NicknameStatus = NicknameStatus.IDLE,
    val isChecking: Boolean = false,
) {
    /** 중복 확인 버튼 활성: 닉네임이 비어있지 않고 검사 중이 아닐 때. */
    val isCheckEnabled: Boolean
        get() = nickname.isNotBlank() && !isChecking

    /** 시작하기 버튼 활성: 사용 가능으로 확인됐을 때만. */
    val isStartEnabled: Boolean
        get() = status == NicknameStatus.AVAILABLE
}

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface SocialNicknameEffect {
    /**
     * 닉네임 확정 후 회원가입 완료 화면으로 이동.
     * 약관 동의는 이 화면 진입 전(TermsAgreementScreen)에서 이미 끝난 상태다.
     *
     * TODO: 실제 온보딩 API(PATCH /api/members/me/onboarding) 연동 시
     *  nickname + 약관 동의 화면에서 넘어온 termAgreements를 함께 실어 호출해야 한다.
     */
    data object NavigateToComplete : SocialNicknameEffect
}
