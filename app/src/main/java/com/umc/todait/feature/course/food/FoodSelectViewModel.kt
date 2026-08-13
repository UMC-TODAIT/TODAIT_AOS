package com.umc.todait.feature.course.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.data.dto.CourseDraftStatus
import com.umc.todait.feature.course.data.repository.CourseDraftRepository
import com.umc.todait.feature.course.data.repository.TaxonomyRepository
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
 * 음식 선택 화면(취향 설정 2/2)의 상태를 관리한다.
 *
 * 진입 시 음식 카테고리 목록을 조회해 카드를 그리고, 분위기 선택 화면에서 발급된 임시 코스
 * (course-draft) 핸들을 nav 인자로 이어받아 헤더 확인(체크) 탭 시 선택값을 저장하고
 * 기준 장소 설정 화면으로 넘어간다.
 */
@HiltViewModel
class FoodSelectViewModel @Inject constructor(
    private val courseDraftRepository: CourseDraftRepository,
    private val taxonomyRepository: TaxonomyRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val courseDraftId: Long = checkNotNull(savedStateHandle[Screen.FoodSelect.ARG_COURSE_DRAFT_ID]) {
        "courseDraftId 인자가 필요합니다."
    }

    private val _uiState = MutableStateFlow(FoodSelectUiState())
    val uiState: StateFlow<FoodSelectUiState> = _uiState.asStateFlow()

    private val _effect = Channel<FoodSelectEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadFoodCategories()
    }

    /**
     * 음식 카테고리 목록 조회(GET /api/food-categories) + 진행 중 임시 코스의 기존 선택값 복원.
     * 에러 화면의 재시도에서도 재사용한다.
     *
     * 기준 장소 화면에서 이전 버튼(`<`)으로 돌아오면 이 화면은 매번 새로 만들어지므로
     * (앞으로 갈 때 navigate 로 새 back stack entry 가 쌓인다) 저장된 선택값을 서버에서 다시 받아온다.
     */
    fun loadFoodCategories() {
        _uiState.update { it.copy(listState = FoodListState.Loading) }
        viewModelScope.launch {
            when (val result = taxonomyRepository.getFoodCategories()) {
                is ApiResult.Success -> {
                    val foods = result.data.foodCategories.map { it.toUiModel() }
                    _uiState.update { it.copy(listState = FoodListState.Success(foods)) }
                    restoreSavedSelection()
                }

                is ApiResult.Failure ->
                    _uiState.update { it.copy(listState = FoodListState.Error(result.toUiError().message)) }
            }
        }
    }

    /**
     * 진행 중인 임시 코스(GET /api/course-drafts/current)에서 기존 선택값을 되살린다.
     *
     * 조회가 실패하거나 진행 중인 임시 코스가 없으면 아무것도 하지 않는다 — 저장된 값을 모르면
     * "바뀐 것으로" 보고 저장 API 를 타므로 동작이 어긋나지 않는다.
     */
    private suspend fun restoreSavedSelection() {
        val draft = (courseDraftRepository.getCurrentCourseDraft() as? ApiResult.Success)?.data ?: return
        // 다른 임시 코스가 진행 중이면(정상 플로우에선 없다) 이 화면이 들고 온 핸들을 신뢰한다.
        if (draft.courseDraftId != courseDraftId) return

        val savedIds = draft.savedFoodCategoryIds.toSet()
        _uiState.update { state ->
            val current = state.listState as? FoodListState.Success ?: return@update state
            state.copy(
                listState = current.copy(
                    foods = current.foods.map { it.copy(isSelected = it.foodCategoryId in savedIds) },
                ),
                savedFoodCategoryIds = savedIds,
                hasSavedPlaces = draft.hasSavedPlaces,
            )
        }
    }

    /** 카드 탭 → 선택 토글. 음식은 상한이 없어 개수 제한 없이 토글한다. */
    fun onToggleFood(code: String) {
        _uiState.update { state ->
            val current = state.listState as? FoodListState.Success ?: return@update state
            state.copy(
                listState = current.copy(
                    foods = current.foods.map {
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
            !state.isSelectionChanged ->
                viewModelScope.launch { _effect.send(FoodSelectEffect.NavigateToBasePlace(courseDraftId)) }

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
     * 이전 버튼(`<`) → 단계 이동 API 호출 후 분위기 선택 화면으로 돌아간다.
     *
     * 단순 화면 이동이라 저장·삭제 API 도, 알림도 없다. 단계 이동 호출이 실패해도 화면은 넘긴다
     * (사용자에게는 뒤로 가기일 뿐이고, 되돌아온 화면에서 확인을 누르면 그때 다시 전이한다).
     */
    fun onBackClick() {
        viewModelScope.launch {
            courseDraftRepository.moveToStatus(courseDraftId, CourseDraftStatus.MOOD_SELECTING)
            _effect.send(FoodSelectEffect.NavigateBack)
        }
    }

    /** 음식 선택 저장 후 기준 장소 설정 화면으로 이동. */
    private fun saveAndNavigate() {
        val selectedIds = _uiState.value.selectedFoodCategoryIds
        // 값이 실제로 바뀐 저장일 때만 서버가 장소 데이터를 초기화한다.
        val resetsPlaces = _uiState.value.isSelectionChanged

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            when (val result = courseDraftRepository.saveFoodCategories(courseDraftId, selectedIds)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            savedFoodCategoryIds = selectedIds.toSet(),
                            hasSavedPlaces = if (resetsPlaces) false else it.hasSavedPlaces,
                        )
                    }
                    _effect.send(FoodSelectEffect.NavigateToBasePlace(courseDraftId))
                }

                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = result.toUiError().message) }
            }
        }
    }
}
