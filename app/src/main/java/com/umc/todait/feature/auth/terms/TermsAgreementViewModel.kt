package com.umc.todait.feature.auth.terms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.feature.auth.data.dto.TermAgreementDto
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
 * GET /api/terms 응답 스펙이 아직 확정되지 않아 우선 더미 약관 목록(DUMMY_TERMS)으로 화면을
 * 완성하고, 스펙이 나오면 서버 목록 조회로 교체한다.
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
        val url = _uiState.value.terms.firstOrNull { it.termId == termId }?.detailUrl ?: return
        viewModelScope.launch {
            _effect.send(TermsAgreementEffect.OpenDetail(url))
        }
    }

    fun onNextClick() {
        val state = _uiState.value
        if (!state.isNextEnabled) return
        viewModelScope.launch {
            _effect.send(
                TermsAgreementEffect.NavigateNext(
                    flow = state.flow,
                    agreedTerms = state.terms.map { TermAgreementDto(termType = it.termType, agreed = it.isAgreed) },
                ),
            )
        }
    }

    private companion object {
        // TODO(BE 고슴이): GET /api/terms 스펙 확정되면 서버 응답으로 교체.
        // 약관 전문은 노션에 올려두고 앱은 링크만 연다(팀 방침 — 공지사항·고객센터도 동일).
        // BE 의 term 테이블 content 컬럼에도 같은 노션 주소가 들어간다.
        val DUMMY_TERMS = listOf(
            TermItemUiModel(
                termId = 1, termType = "SERVICE", title = "서비스 이용약관",
                isRequired = true, isAgreed = false,
                detailUrl = "https://tranquil-paw-d58.notion.site/39bd2aae5cbb8049aa0fc51166ffaf19",
            ),
            TermItemUiModel(
                termId = 2, termType = "PRIVACY", title = "개인정보 수집 및 이용",
                isRequired = true, isAgreed = false,
                detailUrl = "https://tranquil-paw-d58.notion.site/39bd2aae5cbb805bb2abd40d3abc41d3",
            ),
            TermItemUiModel(
                termId = 3, termType = "LOCATION", title = "위치정보 이용 권한",
                isRequired = false, isAgreed = false,
                detailUrl = "https://tranquil-paw-d58.notion.site/39bd2aae5cbb805faceff6d042af9ead",
            ),
            TermItemUiModel(
                termId = 4, termType = "MARKETING", title = "마케팅 푸시 알림",
                isRequired = false, isAgreed = false,
                detailUrl = "https://tranquil-paw-d58.notion.site/39bd2aae5cbb80b88c77c3f895fb280c",
            ),
        )
    }
}
