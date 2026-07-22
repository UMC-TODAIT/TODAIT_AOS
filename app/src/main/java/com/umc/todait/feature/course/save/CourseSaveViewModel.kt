package com.umc.todait.feature.course.save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.feature.course.compose.CourseMood
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
 * 코스 저장 화면의 입력 상태(이름·메모·태그)를 관리한다.
 *
 * 경로 미리보기에 쓰는 장소 목록은 코스 구성 그래프에 스코프된
 * [com.umc.todait.feature.course.compose.CourseComposeViewModel] 이 들고 있으므로 여기서는 다루지 않는다.
 *
 * ⚠️ 코스 저장 API 가 아직 명세에 없어 [onSave] 는 검증만 하고 바로 이동한다. (아래 TODO)
 */
@HiltViewModel
class CourseSaveViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CourseSaveUiState())
    val uiState: StateFlow<CourseSaveUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CourseSaveEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onNameChange(value: String) {
        _uiState.update {
            it.copy(name = value.take(CourseSaveUiState.MAX_NAME_LENGTH))
        }
    }

    fun onMemoChange(value: String) {
        _uiState.update { it.copy(memo = value.take(CourseSaveUiState.MAX_MEMO_LENGTH)) }
    }

    /** '+' 탭 → 태그 추가 바텀시트 열기. 현재 선택을 초안으로 복사한다. */
    fun onStartAddTag() {
        _uiState.update { it.copy(isTagSheetVisible = true, draftTags = it.selectedTags) }
    }

    /** 바텀시트에서 프리셋 태그 탭 → 초안 선택 토글(선택↔해제). 배경색이 회색↔그라데이션으로 바뀐다. */
    fun onToggleTag(mood: CourseMood) {
        _uiState.update {
            val draft = if (mood in it.draftTags) it.draftTags - mood else it.draftTags + mood
            it.copy(draftTags = draft)
        }
    }

    /** 바텀시트 ✓ → 초안 선택을 확정하고 닫는다. */
    fun onConfirmTags() {
        _uiState.update { it.copy(selectedTags = it.draftTags, isTagSheetVisible = false) }
    }

    /** 바텀시트 X(또는 딤 영역 탭) → 초안을 버리고 닫는다. */
    fun onDismissTagSheet() {
        _uiState.update { it.copy(isTagSheetVisible = false, draftTags = emptySet()) }
    }

    /**
     * 헤더 ✓ → 코스 저장 시도. 이름이 비어 있으면 안내 알럿을,
     * 통과하면 "코스를 저장할까요?" 확인 알럿을 띄운다.
     */
    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) {
            _uiState.update { it.copy(isNameErrorDialogVisible = true) }
            return
        }
        _uiState.update { it.copy(isSaveConfirmDialogVisible = true) }
    }

    /** 이름 미입력 안내 알럿 닫기(취소/확인 모두 닫기만 한다). */
    fun onDismissNameErrorDialog() {
        _uiState.update { it.copy(isNameErrorDialogVisible = false) }
    }

    /** 저장 확인 알럿 [확인] → 실제 저장 후 완료 다이얼로그로 넘어간다. */
    fun onConfirmSave() {
        // TODO(BE 죠): 코스 저장 API(이름·메모·태그 + 임시 코스 세션의 장소 순서) 확정 시 연동.
        //  현재는 API 가 없어 검증만 하고 바로 완료 다이얼로그로 넘어간다.
        _uiState.update {
            it.copy(isSaveConfirmDialogVisible = false, isSavedDialogVisible = true)
        }
    }

    /** 저장 확인 알럿 [취소] → 알럿만 닫는다. */
    fun onDismissSaveConfirm() {
        _uiState.update { it.copy(isSaveConfirmDialogVisible = false) }
    }

    /** 완료 다이얼로그 [저장된 코스로 이동하기]. */
    fun onMoveToSavedCourses() {
        _uiState.update { it.copy(isSavedDialogVisible = false) }
        viewModelScope.launch { _effect.send(CourseSaveEffect.NavigateToSavedCourses) }
    }

    /** 완료 다이얼로그 [건너뛰기]. 저장은 이미 끝났으므로 홈으로 보낸다. */
    fun onSkipSavedDialog() {
        _uiState.update { it.copy(isSavedDialogVisible = false) }
        viewModelScope.launch { _effect.send(CourseSaveEffect.NavigateToHome) }
    }
}
