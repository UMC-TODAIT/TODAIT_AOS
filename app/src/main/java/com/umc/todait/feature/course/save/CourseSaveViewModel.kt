package com.umc.todait.feature.course.save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.compose.CourseMood
import com.umc.todait.feature.course.data.repository.CourseDraftRepository
import com.umc.todait.feature.course.data.repository.TaxonomyRepository
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
 * 코스 저장 화면의 입력 상태(이름·메모·태그)와 최종 저장 요청을 관리한다.
 *
 * 경로 미리보기에 쓰는 장소 목록은 코스 구성 그래프에 스코프된
 * [com.umc.todait.feature.course.compose.CourseComposeViewModel] 이 들고 있으므로 여기서는 다루지 않는다.
 * 임시 코스 핸들(courseDraftId)도 같은 이유로 화면에서 인자로 받아 [onConfirmSave] 에 넘긴다.
 *
 * 저장은 POST /api/course-drafts/{courseDraftId}/courses 한 번으로 끝난다. 음식 카테고리·장소·방문 순서는
 * 이미 임시 코스에 저장돼 있어 요청에 넣지 않고, 화면에서 확정한 title·memo·moodTagIds 만 보낸다.
 */
@HiltViewModel
class CourseSaveViewModel @Inject constructor(
    private val courseDraftRepository: CourseDraftRepository,
    private val taxonomyRepository: TaxonomyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseSaveUiState())
    val uiState: StateFlow<CourseSaveUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CourseSaveEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // 화면 프리셋(CourseMood) → 서버 mood_tag.id. 저장 요청의 moodTagIds 를 만들 때 쓴다.
    private var moodTagIdByCode: Map<String, Long> = emptyMap()

    init {
        loadMoodTagIds()
    }

    /**
     * 분위기 태그 기준 데이터 로드(GET /api/mood-tags).
     *
     * 저장 요청은 태그 이름이 아니라 mood_tag.id 를 받으므로 id 를 서버에서 받아 둔다.
     * (컨벤션상 기준 데이터 id 는 앱에 하드코딩하지 않는다.)
     * 실패해도 화면 입력은 막지 않고, 저장 시점에 안내 문구를 띄운다.
     */
    private fun loadMoodTagIds() {
        viewModelScope.launch {
            when (val result = taxonomyRepository.getMoodTags()) {
                is ApiResult.Success ->
                    moodTagIdByCode = result.data.moodTags.associate { it.code to it.moodTagId }

                is ApiResult.Failure -> Unit
            }
        }
    }

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
     * 헤더 ✓ → 코스 저장 시도. 서버가 400 을 주기 전에 화면에서 먼저 걸러낸다.
     *
     * 이름이 비어 있으면 안내 알럿을, 분위기 태그가 2~6개가 아니면 태그 안내 알럿을,
     * 모두 통과하면 "코스를 저장할까요?" 확인 알럿을 띄운다.
     */
    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) {
            _uiState.update { it.copy(isNameErrorDialogVisible = true) }
            return
        }
        if (!state.hasValidTagCount) {
            _uiState.update { it.copy(isTagCountErrorDialogVisible = true) }
            return
        }
        _uiState.update { it.copy(isSaveConfirmDialogVisible = true) }
    }

    /** 이름 미입력 안내 알럿 닫기(취소/확인 모두 닫기만 한다). */
    fun onDismissNameErrorDialog() {
        _uiState.update { it.copy(isNameErrorDialogVisible = false) }
    }

    /** 태그 개수 안내 알럿 닫기. */
    fun onDismissTagCountErrorDialog() {
        _uiState.update { it.copy(isTagCountErrorDialogVisible = false) }
    }

    /**
     * 저장 확인 알럿 [확인] → 코스 저장 요청
     * (POST /api/course-drafts/{courseDraftId}/courses).
     *
     * 같은 임시 코스는 한 번만 저장할 수 있어(재요청 시 COURSE_DRAFT409) [CourseSaveUiState.isSaving]
     * 으로 중복 호출을 막는다. 성공하면 완료 다이얼로그로 넘어간다.
     */
    fun onConfirmSave(courseDraftId: Long) {
        val state = _uiState.value
        if (state.isSaving) return

        val moodTagIds = state.orderedTags.mapNotNull { moodTagIdByCode[it.code] }
        // 기준 데이터 로드가 실패했거나 서버에 없는 태그를 고른 경우. 저장하면 COURSE_MOOD404 가 난다.
        if (moodTagIds.size != state.selectedTags.size) {
            _uiState.update {
                it.copy(isSaveConfirmDialogVisible = false, saveError = MOOD_TAG_UNAVAILABLE_MESSAGE)
            }
            return
        }

        _uiState.update { it.copy(isSaveConfirmDialogVisible = false, isSaving = true, saveError = null) }
        viewModelScope.launch {
            val result = courseDraftRepository.saveCourse(
                courseDraftId = courseDraftId,
                title = state.name,
                memo = state.memo,
                moodTagIds = moodTagIds,
            )
            _uiState.update {
                when (result) {
                    is ApiResult.Success -> it.copy(isSaving = false, isSavedDialogVisible = true)
                    is ApiResult.Failure -> it.copy(isSaving = false, saveError = result.toUiError().message)
                }
            }
        }
    }

    /** 저장 확인 알럿 [취소] → 알럿만 닫는다. */
    fun onDismissSaveConfirm() {
        _uiState.update { it.copy(isSaveConfirmDialogVisible = false) }
    }

    /** 저장 실패 안내 닫기. 화면 입력은 그대로 남아 다시 시도할 수 있다. */
    fun onDismissSaveError() {
        _uiState.update { it.copy(saveError = null) }
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

    private companion object {
        const val MOOD_TAG_UNAVAILABLE_MESSAGE = "분위기 태그 정보를 불러오지 못했어요. 잠시 후 다시 시도해주세요."
    }
}
