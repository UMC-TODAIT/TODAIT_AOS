package com.umc.todait.feature.course.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
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

    init {
        loadMoodTags()
    }

    /** 분위기 태그 목록 조회(GET /api/mood-tags). 에러 화면의 재시도에서도 재사용한다. */
    fun loadMoodTags() {
        _uiState.update { it.copy(listState = MoodListState.Loading) }
        viewModelScope.launch {
            when (val result = taxonomyRepository.getMoodTags()) {
                is ApiResult.Success -> {
                    val moods = result.data.moodTags.map { it.toUiModel() }
                    _uiState.update { it.copy(listState = MoodListState.Success(moods)) }
                }

                is ApiResult.Failure ->
                    _uiState.update { it.copy(listState = MoodListState.Error(result.toUiError().message)) }
            }
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

    /** 헤더 확인(체크) 버튼 → 분위기 선택 저장 후 음식 선택 화면으로 이동. */
    fun onConfirmClick() {
        val state = _uiState.value
        if (!state.isConfirmEnabled) return
        val selectedIds = state.selectedMoodTagIds

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val draftId = ensureCourseDraftId()
            if (draftId == null) {
                _uiState.update { it.copy(isSubmitting = false, submitError = ERROR_DRAFT_REQUIRED) }
                return@launch
            }

            when (val result = courseDraftRepository.saveMoodTags(draftId, selectedIds)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
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
    }
}
