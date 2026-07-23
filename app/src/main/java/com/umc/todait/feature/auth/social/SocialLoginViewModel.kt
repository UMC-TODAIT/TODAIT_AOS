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

sealed interface SocialLoginEffect {
    /** 기존 회원 로그인 완료(토큰 저장됨) → 홈으로 이동. */
    data class Success(val provider: SocialProvider) : SocialLoginEffect

    /** 신규 회원 → 온보딩(약관 동의 → 닉네임 설정) 플로우로 이동. */
    data class NeedsOnboarding(val provider: SocialProvider) : SocialLoginEffect

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
            is ApiResult.Success -> {
                val data = result.data
                when {
                    // onboardingToken 이 있으면 신규 회원 — 온보딩 필요.
                    !data.onboardingToken.isNullOrBlank() ->
                        _effect.send(SocialLoginEffect.NeedsOnboarding(provider))
                    // 기존 회원 — 서비스 토큰 저장 후 로그인 완료.
                    !data.accessToken.isNullOrBlank() && !data.refreshToken.isNullOrBlank() -> {
                        tokenDataStore.saveTokens(data.accessToken, data.refreshToken)
                        _effect.send(SocialLoginEffect.Success(provider))
                    }
                    else -> _effect.send(SocialLoginEffect.Failure("로그인 응답이 올바르지 않아요."))
                }
            }

            is ApiResult.Failure ->
                _effect.send(SocialLoginEffect.Failure(result.toUiError().message))
        }
    }
}
