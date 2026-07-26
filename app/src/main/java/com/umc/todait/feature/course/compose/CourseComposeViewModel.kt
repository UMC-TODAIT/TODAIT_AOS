package com.umc.todait.feature.course.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.base_place.PlaceUiModel
import com.umc.todait.feature.course.base_place.toUiModel
import com.umc.todait.feature.course.data.repository.CourseDraftRepository
import com.umc.todait.feature.course.data.repository.PlaceCategoryRepository
import com.umc.todait.feature.course.data.repository.RecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 코스 구성하기 플로우(#26)의 **공유** 상태를 관리한다(선택 화면 + 선택한 장소 화면).
 * NavHost 의 course/compose 중첩 그래프에 스코프되어 두 화면이 같은 인스턴스를 쓴다.
 *
 * 진입 시 선택된 카테고리(기본 카페) 기준으로 기준 장소 주변 추천 장소를 불러오고, 카드 '+' 로 코스에 담는다.
 * 화면 전환(✓)은 네비게이션이라 UI(콜백)에서 처리하고, ViewModel 은 순수 상태만 들고 있다.
 *
 * ⚠️ 기준 장소/임시 코스 세션(courseDraftId·basePlaceId)은 세션 API 연동 시 채운다. (아래 TODO)
 */
@HiltViewModel
class CourseComposeViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val placeCategoryRepository: PlaceCategoryRepository,
    private val courseDraftRepository: CourseDraftRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseComposeUiState())
    val uiState: StateFlow<CourseComposeUiState> = _uiState.asStateFlow()

    init {
        // 카테고리(탭)가 있어야 추천을 조회할 수 있어(placeCategoryCode 필요) 카테고리 로드 후 추천을 부른다.
        // TODO(2차): 임시 코스는 원래 코스 생성 진입(분위기 선택) 시점에 만들어 플로우 전체가 공유해야 한다.
        //  base-place 저장 API 배포 전까지는 구성 화면 진입 시 발급해 courseDraftId 핸들을 확보한다.
        loadCategories()
    }

    /**
     * 임시 코스 생성(POST /api/course-drafts). 성공 시 courseDraftId 를 상태에 보관한다.
     * 이미 발급받았으면 그대로 재사용한다.
     */
    private suspend fun ensureCourseDraftId(): Long? {
        _uiState.value.courseDraftId?.let { return it }
        return when (val result = courseDraftRepository.createCourseDraft()) {
            is ApiResult.Success -> result.data.courseDraftId.also { id ->
                _uiState.update { it.copy(courseDraftId = id) }
            }

            is ApiResult.Failure -> null
        }
    }

    /** 에러 화면의 [다시 시도]. 카테고리부터 다시 불러오고 이어서 추천을 조회한다. */
    fun retry() {
        _uiState.update { it.copy(recommendState = RecommendListState.Loading) }
        loadCategories()
    }

    /** 카테고리 탭(장소 대분류) 로드(GET /api/place-categories). sortOrder 순으로 노출. */
    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = placeCategoryRepository.getPlaceCategories()) {
                is ApiResult.Success -> {
                    val categories = result.data.placeCategories
                        .sortedBy { it.sortOrder }
                        .map { it.toUiModel() }
                    _uiState.update { state ->
                        state.copy(
                            categories = categories,
                            // 첫 진입 시 첫 카테고리를 기본 선택(없으면 null).
                            selectedCategoryId = state.selectedCategoryId ?: categories.firstOrNull()?.id,
                        )
                    }
                    if (categories.isEmpty()) {
                        // 명세상 카테고리가 없어도 오류가 아니라 빈 배열로 내려온다 → 빈 상태로 처리.
                        _uiState.update { it.copy(recommendState = RecommendListState.Empty(EMPTY_MESSAGE)) }
                    } else {
                        loadRecommendations()
                    }
                }

                // 카테고리(placeCategoryCode)가 없으면 추천을 조회할 수 없으므로 목록도 에러로 표시한다.
                is ApiResult.Failure ->
                    _uiState.update {
                        it.copy(recommendState = RecommendListState.Error(result.toUiError().message))
                    }
            }
        }
    }

    /**
     * 현재 선택된 카테고리 기준 추천 장소 조회. 재시도에서도 재사용한다.
     * (GET /api/course-drafts/{courseDraftId}/recommended-places?placeCategoryCode=)
     */
    fun loadRecommendations() {
        viewModelScope.launch {
            // 카테고리 로드 전이면 로드 완료 콜백에서 다시 호출된다.
            val categoryCode = _uiState.value.selectedCategory?.code ?: return@launch

            _uiState.update { it.copy(recommendState = RecommendListState.Loading) }
            val draftId = ensureCourseDraftId()
            if (draftId == null) {
                _uiState.update {
                    it.copy(recommendState = RecommendListState.Error(ERROR_DRAFT_REQUIRED))
                }
                return@launch
            }

            val result = recommendationRepository.getRecommendedPlaces(
                courseDraftId = draftId,
                placeCategoryCode = categoryCode,
            )
            _uiState.update { current ->
                val next = when (result) {
                    is ApiResult.Success -> {
                        val places = result.data.places.map { it.toUiModel() }
                        if (places.isEmpty()) {
                            RecommendListState.Empty(EMPTY_MESSAGE)
                        } else {
                            RecommendListState.Success(places)
                        }
                    }

                    is ApiResult.Failure ->
                        RecommendListState.Error(result.toUiError().message)
                }
                current.copy(recommendState = next)
            }
        }
    }

    /** 카테고리 탭 선택. 선택 카테고리 기준으로 추천 목록을 다시 불러온다. */
    fun onSelectCategory(categoryId: Long) {
        if (_uiState.value.selectedCategoryId == categoryId) return
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadRecommendations()
    }

    /**
     * 추천 카드 '+' → 코스에 담기.
     * 이미 담긴 장소면 중복 알럿을 띄우고, 아니면 목록 끝에 추가한다.
     */
    fun onAddPlace(place: PlaceUiModel) {
        _uiState.update { state ->
            if (state.selectedPlaces.any { it.key == place.key }) {
                state.copy(alert = CourseComposeAlert.Duplicate)
            } else {
                state.copy(selectedPlaces = state.selectedPlaces + place)
            }
        }
    }

    /** 선택한 장소에서 빼기. */
    fun onRemovePlace(place: PlaceUiModel) {
        _uiState.update { state ->
            state.copy(selectedPlaces = state.selectedPlaces.filterNot { it.key == place.key })
        }
    }

    /**
     * 선택한 장소 순서 변경(드래그).
     * TODO: 드래그 제스처(reorderable) 연동. 현재는 인덱스 이동 로직만 제공.
     */
    fun onMovePlace(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val list = state.selectedPlaces.toMutableList()
            if (fromIndex !in list.indices || toIndex !in list.indices) return@update state
            list.add(toIndex, list.removeAt(fromIndex))
            state.copy(selectedPlaces = list)
        }
    }

    fun onDismissAlert() {
        _uiState.update { it.copy(alert = null) }
    }

    companion object {
        private const val EMPTY_MESSAGE = "추천할 수 있는 장소가 없어요."
        private const val ERROR_DRAFT_REQUIRED = "코스 생성 정보를 불러오지 못했어요. 다시 시도해주세요."
    }
}
