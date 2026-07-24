package com.umc.todait.feature.auth.terms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 약관 동의 화면의 상태를 관리한다.
 *
 * GET /api/terms, GET /api/terms/{termId}는 폐기됐다(노션 문서로 이동, API 없음) — 약관 목록은
 * 서버 조회 없이 앱에 영구 고정(DUMMY_TERMS)한다.
 */
@HiltViewModel
class TermsAgreementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(
        TermsAgreementUiState(
            flow = TermsFlow.fromRoute(savedStateHandle[Screen.TermsAgreement.ARG_FLOW]),
            terms = DUMMY_TERMS,
        ),
    )
    val uiState: StateFlow<TermsAgreementUiState> = _uiState.asStateFlow()

    private val _effect = Channel<TermsAgreementEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /** 전체 동의 토글. 이미 전체 동의 상태면 전체 해제, 아니면 전체 동의로 바꾼다. */
    fun onToggleAll() {
        _uiState.update { state ->
            val nextAgreed = !state.isAllAgreed
            state.copy(terms = state.terms.map { it.copy(isAgreed = nextAgreed) })
        }
    }

    fun onToggleTerm(termId: Long) {
        _uiState.update { state ->
            state.copy(
                terms = state.terms.map {
                    if (it.termId == termId) it.copy(isAgreed = !it.isAgreed) else it
                },
            )
        }
    }

    /** 필수 약관 항목의 화살표 탭 → 상세 화면 이동(체크 토글과는 별개 동작). */
    fun onViewDetail(termId: Long) {
        viewModelScope.launch {
            _effect.send(TermsAgreementEffect.NavigateToDetail(termId))
        }
    }

    fun onNextClick() {
        val state = _uiState.value
        if (!state.isNextEnabled) return
        viewModelScope.launch {
            _effect.send(
                TermsAgreementEffect.NavigateNext(
                    flow = state.flow,
                    agreedTerms = state.terms,
                ),
            )
        }
    }

    private companion object {
        // GET /api/terms 폐기(노션 이동)로 서버 목록 조회가 없어 영구 고정한다.
        val DUMMY_TERMS = listOf(
            TermItemUiModel(
                termId = 1, termType = "SERVICE", title = "서비스 이용약관",
                isRequired = true, isAgreed = false, hasDetail = true,
            ),
            TermItemUiModel(
                termId = 2, termType = "PRIVACY", title = "개인정보 수집 및 이용",
                isRequired = true, isAgreed = false, hasDetail = true,
            ),
            TermItemUiModel(
                termId = 3, termType = "LOCATION", title = "위치정보 이용 권한",
                isRequired = false, isAgreed = false,
            ),
            TermItemUiModel(
                termId = 4, termType = "MARKETING", title = "마케팅 푸시 알림",
                isRequired = false, isAgreed = false,
            ),
        )
    }
}
