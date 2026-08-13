package com.umc.todait.feature.course.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.data.dto.CourseDraftStatus
import com.umc.todait.feature.course.data.dto.CurrentCourseDraftResponseDto
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
 * 분위기 선택 화면(취향 설정 1/2)의 상태를 관리한다.
 *
 * 진입 시 분위기 태그 목록을 조회해 카드를 그리고, 헤더 확인(체크) 탭 시 임시 코스(course-draft)를
 * 아직 없으면 새로 발급하고 선택값을 저장한 뒤 음식 선택 화면으로 넘어간다.
 * 코스 생성 플로우의 진입점이라 courseDraftId 발급도 이 화면이 담당한다.
 */
@HiltViewModel
class MoodSelectViewModel @Inject constructor(
    private val courseDraftRepository: CourseDraftRepository,
    private val taxonomyRepository: TaxonomyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodSelectUiState())
    val uiState: StateFlow<MoodSelectUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MoodSelectEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // 코스 생성 플로우 전체가 공유하는 임시 코스 핸들. 이 화면에서 최초 발급된다.
    private var courseDraftId: Long? = null

    // 진입 시 조회한 "진행 중인 임시 코스". 이어서 하기/새로 만들기 선택에 쓰고,
    // 이어서 할 때는 이 응답으로 화면을 복원해 current 를 다시 부르지 않는다.
    private var inProgressDraft: CurrentCourseDraftResponseDto? = null

    // 첫 ON_RESUME 은 진입 조회와 겹쳐 무의미하다. [onScreenResumed] 참고.
    private var isFirstResume = true

    init {
        loadMoodTags()
    }

    /**
     * 분위기 태그 목록 조회(GET /api/mood-tags) + 진행 중 임시 코스의 기존 선택값 복원.
     * 에러 화면의 재시도에서도 재사용한다.
     *
     * 목록을 먼저 그리고 그 위에 기존 선택값을 덧입힌다. 이전 버튼(`<`)으로 돌아왔을 때
     * 저장했던 무드가 선택된 채로 보여야 하기 때문이다.
     */
    fun loadMoodTags() {
        _uiState.update { it.copy(listState = MoodListState.Loading) }
        viewModelScope.launch {
            when (val result = taxonomyRepository.getMoodTags()) {
                is ApiResult.Success -> {
                    val moods = result.data.moodTags.map { it.toUiModel() }
                    _uiState.update { it.copy(listState = MoodListState.Success(moods)) }
                    checkInProgressDraft()
                }

                is ApiResult.Failure ->
                    _uiState.update { it.copy(listState = MoodListState.Error(result.toUiError().message)) }
            }
        }
    }

    /**
     * 코스 만들기 진입 시 진행 중인 임시 코스가 있는지 확인한다(GET /api/course-drafts/current).
     *
     * 있으면 "이어서 하기 / 새로 만들기"를 묻는다. 없으면(성공 + null) 지금까지처럼 새로 시작하고,
     * 임시 코스는 확인(✅) 시점에 발급한다. 조회가 실패해도 화면을 막지 않는다 — 그냥 새로 시작한다.
     *
     * 이전 버튼(`<`)으로 이 화면에 돌아온 경우에는 back stack entry 가 살아 있어 ViewModel 이
     * 재생성되지 않으므로 여기까지 오지 않는다(=되돌아올 때마다 묻지 않는다).
     */
    private suspend fun checkInProgressDraft() {
        // 이 플로우에서 이미 쓰고 있는 임시 코스가 있으면 물을 이유가 없다.
        if (courseDraftId != null) return
        val draft = (courseDraftRepository.getCurrentCourseDraft() as? ApiResult.Success)?.data ?: return
        inProgressDraft = draft
        _uiState.update { it.copy(showResumePrompt = true) }
    }

    /**
     * [이어서 하기] → 임시 코스를 그대로 이어서 쓴다.
     *
     * 멈춰 있던 단계가 분위기 선택이면 이 화면에서 선택값만 되살리고, 그 이후 단계면
     * 해당 화면으로 이동한다. current 응답을 이미 들고 있어 다시 조회하지 않는다.
     */
    fun onResumeContinue() {
        val draft = inProgressDraft ?: return
        _uiState.update { it.copy(showResumePrompt = false) }
        courseDraftId = draft.courseDraftId

        // 뒤 단계로 점프하더라도 선택값은 먼저 입힌다. 이 화면은 스택 맨 아래에 남아 있어
        // 사용자가 이전 버튼으로 되돌아오는데, 그때 비어 있으면 저장했던 무드가 사라진 것처럼
        // 보이고 hasSavedPlaces 도 모르는 상태라 초기화 알림까지 건너뛰게 된다.
        applySavedSelection(draft)

        val status = draft.status ?: CourseDraftStatus.MOOD_SELECTING
        if (status == CourseDraftStatus.MOOD_SELECTING) return

        viewModelScope.launch {
            _effect.send(
                MoodSelectEffect.NavigateToStep(
                    status = status,
                    courseDraftId = draft.courseDraftId,
                    basePlaceId = draft.basePlaceId,
                ),
            )
        }
    }

    /**
     * [새로 만들기] → 기존 임시 코스를 포기하고 새 임시 코스로 처음부터 시작한다.
     *
     * ⚠️ 포기(DELETE)가 성공한 뒤에만 새로 만든다(POST). 실패하면 기존 임시 코스가 그대로 남으므로
     * 새로 만들지 않고 안내만 띄운다 — 그냥 넘어가면 지워진 줄 알았던 코스가 다음 진입에 또 나온다.
     */
    fun onStartNew() {
        val draft = inProgressDraft ?: return
        _uiState.update { it.copy(showResumePrompt = false, isSubmitting = true, resumeError = null) }

        viewModelScope.launch {
            val abandoned = courseDraftRepository.abandonCourseDraft(draft.courseDraftId)
            if (abandoned is ApiResult.Failure) {
                _uiState.update {
                    it.copy(isSubmitting = false, showResumePrompt = true, resumeError = ERROR_ABANDON_FAILED)
                }
                return@launch
            }

            inProgressDraft = null
            when (val created = courseDraftRepository.createCourseDraft()) {
                is ApiResult.Success -> {
                    courseDraftId = created.data.courseDraftId
                    _uiState.update { it.copy(isSubmitting = false) }
                }

                is ApiResult.Failure ->
                    // 임시 코스는 확인(✅) 시점에 다시 발급을 시도하므로 화면은 그대로 쓸 수 있다.
                    _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    /** 포기 실패 안내를 닫는다. 안내를 닫으면 다시 "이어서 하기 / 새로 만들기"를 고를 수 있다. */
    fun onDismissResumeError() {
        _uiState.update { it.copy(resumeError = null) }
    }

    /**
     * 화면으로 되돌아왔을 때 저장 기준값(저장된 무드 · 장소 보유 여부)만 다시 받아온다.
     *
     * ⚠️ 이 화면은 뒤로 가기로 돌아와도 back stack entry 가 살아 있어 ViewModel 이 재생성되지
     * 않는다. 그래서 진입 때 한 번 잡은 기준값이 그대로 남는데, 그 사이 사용자가 뒤 단계에서
     * 기준 장소를 정했다면 hasSavedPlaces 가 거짓으로 낡는다. 그 상태로 무드를 바꾸면
     * 초기화 알림 없이 저장이 나가 서버가 장소를 지운다(에뮬레이터에서 재현). 그래서 갱신한다.
     *
     * 화면에 보이는 선택 상태는 건드리지 않는다 — 사용자가 바꿔둔 걸 덮으면 안 된다.
     */
    fun onScreenResumed() {
        // 첫 진입은 loadMoodTags → checkInProgressDraft 가 이미 처리한다. 중복 조회를 피한다.
        if (isFirstResume) {
            isFirstResume = false
            return
        }
        val draftId = courseDraftId ?: return
        viewModelScope.launch {
            val draft = (courseDraftRepository.getCurrentCourseDraft() as? ApiResult.Success)?.data ?: return@launch
            if (draft.courseDraftId != draftId) return@launch
            _uiState.update {
                it.copy(
                    savedMoodTagIds = draft.savedMoodTagIds.toSet(),
                    hasSavedPlaces = draft.hasSavedPlaces,
                )
            }
        }
    }

    /** current 응답의 저장된 분위기 선택을 화면에 입힌다. */
    private fun applySavedSelection(draft: CurrentCourseDraftResponseDto) {
        val savedIds = draft.savedMoodTagIds.toSet()
        _uiState.update { state ->
            val current = state.listState as? MoodListState.Success ?: return@update state
            state.copy(
                listState = current.copy(
                    moods = current.moods.map { it.copy(isSelected = it.moodTagId in savedIds) },
                ),
                savedMoodTagIds = savedIds,
                hasSavedPlaces = draft.hasSavedPlaces,
            )
        }
    }

    /** 카드 탭 → 선택 토글. 해제는 항상 허용하고, 추가 선택만 상한(6개)에서 막는다. */
    fun onToggleMood(code: String) {
        _uiState.update { state ->
            val current = state.listState as? MoodListState.Success ?: return@update state
            val target = current.moods.firstOrNull { it.code == code } ?: return@update state
            if (!target.isSelected && state.selectedCount >= MoodSelectUiState.MAX_SELECTION) return@update state

            state.copy(
                listState = current.copy(
                    moods = current.moods.map {
                        if (it.code == code) it.copy(isSelected = !it.isSelected) else it
                    },
                ),
                submitError = null,
            )
        }
    }

    /**
     * 헤더 확인(체크) 버튼. 실제 변경은 여기서만 확정된다(카드 탭은 화면 내 선택 상태만 바꾼다).
     *
     * - 저장된 값과 같으면: 저장 API 없이 그대로 다음 단계로 이동
     * - 달라졌고 저장된 장소가 없으면: 알림 없이 저장 후 이동
     * - 달라졌고 저장된 장소가 있으면: 초기화 확인 알림 (서버가 장소 데이터를 초기화한다)
     */
    fun onConfirmClick() {
        val state = _uiState.value
        if (!state.isConfirmEnabled) return

        when {
            !state.isSelectionChanged -> navigateWithoutSaving()
            state.needsResetConfirm -> _uiState.update { it.copy(showResetAlert = true) }
            else -> saveAndNavigate()
        }
    }

    /** 초기화 확인 알림 [확인] → 저장 API 호출 후 다음 단계로 이동. */
    fun onResetAlertConfirm() {
        _uiState.update { it.copy(showResetAlert = false) }
        saveAndNavigate()
    }

    /** 초기화 확인 알림 [취소] → 저장하지 않고 현재 화면 유지. 선택 상태는 사용자가 바꾼 그대로 둔다. */
    fun onResetAlertDismiss() {
        _uiState.update { it.copy(showResetAlert = false) }
    }

    /**
     * 선택값이 그대로일 때.
     *
     * ⚠️ 이슈 #105 는 "동일하면 저장 없이 다음 단계로 이동" 이지만 배포 서버에서는 그렇게 할 수
     * 없다 — 단계를 앞으로 넘기는 수단이 저장 API 뿐이다. PATCH /status 로 전진 전이를 시도하면
     * COURSE_DRAFT409("현재 임시 코스 상태에서는 요청을 처리할 수 없습니다") 로 막히고(에뮬레이터 확인),
     * 저장도 전이도 안 하고 화면만 넘기면 서버가 이전 단계에 남아 다음 화면의 저장이 409 가 된다.
     * 그래서 값이 같아도 저장 API 를 그대로 부른다.
     */
    private fun navigateWithoutSaving() {
        val draftId = courseDraftId
        if (draftId == null) {
            saveAndNavigate()
            return
        }
        saveAndNavigate()
    }

    /** 분위기 선택 저장 후 음식 선택 화면으로 이동. */
    private fun saveAndNavigate() {
        val selectedIds = _uiState.value.selectedMoodTagIds
        // 값이 실제로 바뀐 저장일 때만 서버가 장소 데이터를 초기화한다.
        val resetsPlaces = _uiState.value.isSelectionChanged

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val draftId = ensureCourseDraftId()
            if (draftId == null) {
                _uiState.update { it.copy(isSubmitting = false, submitError = ERROR_DRAFT_REQUIRED) }
                return@launch
            }

            when (val result = courseDraftRepository.saveMoodTags(draftId, selectedIds)) {
                is ApiResult.Success -> {
                    // 저장에 성공했으니 기준값도 옮겨둔다. 다시 돌아와 그대로 확인하면 재저장하지 않는다.
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            savedMoodTagIds = selectedIds.toSet(),
                            hasSavedPlaces = if (resetsPlaces) false else it.hasSavedPlaces,
                        )
                    }
                    _effect.send(MoodSelectEffect.NavigateToFood(draftId))
                }

                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = result.toUiError().message) }
            }
        }
    }

    /**
     * 임시 코스 핸들 확보. 음식 선택 화면에서 뒤로 돌아와 다시 저장하는 경우를 위해
     * 한 번 발급받은 값은 그대로 재사용한다. (명세상 분위기 저장은 MOOD_SELECTING·FOOD_SELECTING 모두 허용)
     */
    private suspend fun ensureCourseDraftId(): Long? {
        courseDraftId?.let { return it }
        return when (val result = courseDraftRepository.createCourseDraft()) {
            is ApiResult.Success -> result.data.courseDraftId.also { courseDraftId = it }
            is ApiResult.Failure -> null
        }
    }

    private companion object {
        const val ERROR_DRAFT_REQUIRED = "코스 생성 정보를 불러오지 못했어요. 다시 시도해주세요."
        const val ERROR_ABANDON_FAILED = "기존 코스를 정리하지 못했어요. 다시 시도해주세요."
    }
}
