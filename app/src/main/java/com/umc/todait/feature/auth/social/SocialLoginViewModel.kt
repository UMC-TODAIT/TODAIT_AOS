package com.umc.todait.feature.auth.social

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.datastore.TokenDataStore
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.auth.data.dto.SocialLoginResultDto
import com.umc.todait.feature.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SocialProvider { KAKAO, GOOGLE }

/**
 * 소셜 로그인 응답의 `loginStatus`. 명세가 정의한 두 값만 인정한다.
 * (카카오/구글 로그인 API 명세 "비고 — 프론트는 응답의 loginStatus에 따라 처리합니다")
 */
enum class SocialLoginStatus {
    /** 기존 회원. accessToken/refreshToken 저장 후 홈으로. */
    LOGIN_COMPLETED,

    /** 신규 회원. onboardingToken 을 들고 약관·닉네임 온보딩으로. */
    ONBOARDING_REQUIRED,
    ;

    companion object {
        /** 명세에 없는 값이나 null 은 판별 불가로 두어 호출부가 실패로 처리하게 한다. */
        fun from(raw: String?): SocialLoginStatus? = entries.firstOrNull { it.name == raw }
    }
}

sealed interface SocialLoginEffect {
    /** 기존 회원 로그인 완료(토큰 저장됨) → 홈으로 이동. */
    data class Success(val provider: SocialProvider) : SocialLoginEffect

    /**
     * 신규 회원 → 온보딩(약관 동의 → 닉네임 설정) 플로우로 이동. onboardingToken은 온보딩 완료 API 호출까지 들고 가야 한다.
     * [profileImageUrl]은 닉네임 설정 화면에 표시할 소셜 프로필 사진으로, null이면 투데잇 기본 이미지를 쓴다.
     */
    data class NeedsOnboarding(
        val provider: SocialProvider,
        val onboardingToken: String,
        val profileImageUrl: String?,
    ) : SocialLoginEffect

    data class Failure(val message: String) : SocialLoginEffect
}

data class SocialLoginUiState(val isLoading: Boolean = false)

/**
 * 카카오/구글 소셜 로그인 진입점.
 *
 * ① SDK 로그인으로 provider 토큰(카카오 accessToken / 구글 idToken)을 받고
 * ② 그 토큰을 백엔드(POST /api/auth/{kakao,google}/login)로 넘겨 서비스 토큰을 발급받는다.
 * ③ 응답의 onboardingToken 유무로 신규/기존 회원을 판단한다 — 기존 회원이면 토큰을 저장하고 홈으로,
 *    신규 회원이면 온보딩 플로우로 보낸다.
 *
 * 참고: 신규 회원의 온보딩 완료(PATCH /api/members/me/onboarding) 호출은 GET /api/terms(약관 목록) 확정 후
 * 별도로 연결한다 — 지금은 온보딩 화면 진입까지만 담당한다.
 */
@HiltViewModel
class SocialLoginViewModel @Inject constructor(
    private val kakaoLoginManager: KakaoLoginManager,
    private val googleLoginManager: GoogleLoginManager,
    private val authRepository: AuthRepository,
    private val tokenDataStore: TokenDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialLoginUiState())
    val uiState: StateFlow<SocialLoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SocialLoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // SDK 로그인 UI(카카오톡/웹, 구글 다이얼로그)는 Activity 컨텍스트가 필요하므로 화면에서 넘겨받는다.
    fun loginWithKakao(context: Context) = login(SocialProvider.KAKAO) {
        val accessToken = kakaoLoginManager.login(context).getOrElse { return@login Result.failure(it) }.accessToken
        Result.success(authRepository.loginWithKakao(accessToken))
    }

    fun loginWithGoogle(context: Context) = login(SocialProvider.GOOGLE) {
        val idToken = googleLoginManager.login(context).getOrElse { return@login Result.failure(it) }.idToken
        Result.success(authRepository.loginWithGoogle(idToken))
    }

    /**
     * [backendLogin]은 SDK 로그인 → 백엔드 로그인까지 수행한다.
     * SDK 단계 실패는 Result.failure, 백엔드 호출까지 갔으면 Result.success(ApiResult) 로 감싸 반환한다.
     */
    private fun login(provider: SocialProvider, backendLogin: suspend () -> Result<ApiResult<SocialLoginResultDto>>) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val outcome = backendLogin()
            _uiState.update { it.copy(isLoading = false) }
            outcome
                .onSuccess { apiResult -> handleBackendResult(provider, apiResult) }
                .onFailure { _effect.send(SocialLoginEffect.Failure(it.message ?: "소셜 로그인에 실패했어요.")) }
        }
    }

    private suspend fun handleBackendResult(provider: SocialProvider, result: ApiResult<SocialLoginResultDto>) {
        when (result) {
            is ApiResult.Success -> handleLoginStatus(provider, result.data)

            is ApiResult.Failure ->
                _effect.send(SocialLoginEffect.Failure(result.toUiError().message))
        }
    }

    /**
     * 명세대로 [SocialLoginResultDto.loginStatus] 로 신규/기존을 가른다.
     *
     * 토큰 유무는 분기 기준이 아니라 **검증**으로만 쓴다 — 상태와 맞지 않는 응답(예: ONBOARDING_REQUIRED
     * 인데 onboardingToken 이 비어 있음)은 진행할 수 없으므로 실패로 드러낸다.
     * 명세에 없는 상태가 오면 조용히 넘어가지 않고 실패 처리해, 서버가 상태를 추가했을 때 바로 알아채도록 한다.
     */
    private suspend fun handleLoginStatus(provider: SocialProvider, data: SocialLoginResultDto) {
        when (SocialLoginStatus.from(data.loginStatus)) {
            SocialLoginStatus.ONBOARDING_REQUIRED -> {
                val onboardingToken = data.onboardingToken
                if (onboardingToken.isNullOrBlank()) {
                    _effect.send(SocialLoginEffect.Failure(ERROR_INVALID_RESPONSE))
                    return
                }
                _effect.send(
                    SocialLoginEffect.NeedsOnboarding(
                        provider = provider,
                        onboardingToken = onboardingToken,
                        profileImageUrl = data.profileImageUrl,
                    ),
                )
            }

            SocialLoginStatus.LOGIN_COMPLETED -> {
                val accessToken = data.accessToken
                val refreshToken = data.refreshToken
                if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                    _effect.send(SocialLoginEffect.Failure(ERROR_INVALID_RESPONSE))
                    return
                }
                tokenDataStore.saveTokens(accessToken, refreshToken)
                _effect.send(SocialLoginEffect.Success(provider))
            }

            null -> _effect.send(SocialLoginEffect.Failure(ERROR_INVALID_RESPONSE))
        }
    }

    private companion object {
        const val ERROR_INVALID_RESPONSE = "로그인 응답이 올바르지 않아요."
    }
}
