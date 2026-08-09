package com.umc.todait.feature.course.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
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

    /** 음식 카테고리 목록 조회(GET /api/food-categories). 에러 화면의 재시도에서도 재사용한다. */
    fun loadFoodCategories() {
        _uiState.update { it.copy(listState = FoodListState.Loading) }
        viewModelScope.launch {
            when (val result = taxonomyRepository.getFoodCategories()) {
                is ApiResult.Success -> {
                    val foods = result.data.foodCategories.map { it.toUiModel() }
                    _uiState.update { it.copy(listState = FoodListState.Success(foods)) }
                }

                is ApiResult.Failure ->
                    _uiState.update { it.copy(listState = FoodListState.Error(result.toUiError().message)) }
            }
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

    /** 헤더 확인(체크) 버튼 → 음식 선택 저장 후 기준 장소 설정 화면으로 이동. */
    fun onConfirmClick() {
        val state = _uiState.value
        if (!state.isConfirmEnabled) return
        val selectedIds = state.selectedFoodCategoryIds

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            when (val result = courseDraftRepository.saveFoodCategories(courseDraftId, selectedIds)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _effect.send(FoodSelectEffect.NavigateToBasePlace(courseDraftId))
                }

                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = result.toUiError().message) }
            }
        }
    }
}
