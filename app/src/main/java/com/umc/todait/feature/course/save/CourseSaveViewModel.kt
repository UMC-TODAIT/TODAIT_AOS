package com.umc.todait.feature.course.save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            it.copy(name = value.take(CourseSaveUiState.MAX_NAME_LENGTH), nameError = null)
        }
    }

    fun onMemoChange(value: String) {
        _uiState.update { it.copy(memo = value.take(CourseSaveUiState.MAX_MEMO_LENGTH)) }
    }

    /** '+' 탭 → 태그 입력창 열기. 이미 최대 개수면 무시한다. */
    fun onStartAddTag() {
        _uiState.update { if (it.canAddTag) it.copy(pendingTag = "") else it }
    }

    fun onPendingTagChange(value: String) {
        // '#' 은 화면에서 붙여주므로 입력값에서는 제거하고, 태그 안에 공백은 허용하지 않는다.
        val cleaned = value.replace("#", "").replace(" ", "")
        _uiState.update {
            it.copy(pendingTag = cleaned.take(CourseSaveUiState.MAX_TAG_LENGTH))
        }
    }

    /** 태그 입력 완료(키보드 Done). 비어 있거나 중복이면 추가하지 않고 입력창만 닫는다. */
    fun onConfirmTag() {
        _uiState.update { state ->
            val tag = state.pendingTag?.trim().orEmpty()
            val isAddable = tag.isNotEmpty() && state.canAddTag && tag !in state.tags
            state.copy(
                tags = if (isAddable) state.tags + tag else state.tags,
                pendingTag = null,
            )
        }
    }

    /** 칩 탭 → 태그 삭제. */
    fun onRemoveTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags - tag) }
    }

    /**
     * 헤더 ✓ → 코스 저장. 이름이 비어 있으면 안내 문구만 노출하고,
     * 통과하면 저장 완료 다이얼로그를 띄운다.
     */
    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) {
            _uiState.update { it.copy(nameError = NAME_REQUIRED_MESSAGE) }
            return
        }
        // TODO(BE 죠): 코스 저장 API(이름·메모·태그 + 임시 코스 세션의 장소 순서) 확정 시 연동.
        //  현재는 API 가 없어 검증만 하고 바로 완료 다이얼로그로 넘어간다.
        _uiState.update { it.copy(isSavedDialogVisible = true) }
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

    companion object {
        // 명세 문구(Figma: 이름 미입력 상태 저장 시도).
        private const val NAME_REQUIRED_MESSAGE = "코스 이름을 입력해주세요."
    }
}
