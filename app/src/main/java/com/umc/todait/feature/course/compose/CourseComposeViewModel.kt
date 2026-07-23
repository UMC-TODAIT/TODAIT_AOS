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
        // TODO(2차): 임시 코스는 원래 코스 생성 진입(기준 장소 확정) 시점에 만들어야 한다.
        //  base-place 저장 API 배포 전까지는 구성 화면 진입 시 발급해 courseDraftId 핸들을 확보한다.
        createCourseDraft()
        loadCategories()
        loadRecommendations()
    }

    /** 임시 코스 생성(POST /api/course-drafts). 성공 시 courseDraftId 를 상태에 보관한다. */
    private fun createCourseDraft() {
        viewModelScope.launch {
            when (val result = courseDraftRepository.createCourseDraft()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(courseDraftId = result.data.courseDraftId) }
                // draft 발급 실패는 화면을 막지 않는다(추천/카테고리는 draft 없이도 조회). 로그만 남기고 2차에서 재시도 UX 보강.
                is ApiResult.Failure -> Unit
            }
        }
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
                }
                // 카테고리 로드 실패 시 탭만 비고 추천 목록은 그대로 노출.
                is ApiResult.Failure -> Unit
            }
        }
    }

    /** 현재 선택된 카테고리 기준 추천 장소 조회. 재시도에서도 재사용한다. */
    fun loadRecommendations() {
        _uiState.update { it.copy(recommendState = RecommendListState.Loading) }
        viewModelScope.launch {
            // TODO(BE 죠): 임시 코스 세션 확정 시 basePlaceId·courseDraftId 와 카테고리 파라미터 전달.
            val result = recommendationRepository.getRecommendedPlaces(
                type = RECOMMENDATION_TYPE_COURSE,
            )
            _uiState.update { state ->
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
                state.copy(recommendState = next)
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
            if (state.selectedPlaces.any { it.placeId == place.placeId }) {
                state.copy(alert = CourseComposeAlert.Duplicate)
            } else {
                state.copy(selectedPlaces = state.selectedPlaces + place)
            }
        }
    }

    /** 선택한 장소에서 빼기. */
    fun onRemovePlace(place: PlaceUiModel) {
        _uiState.update { state ->
            state.copy(selectedPlaces = state.selectedPlaces.filterNot { it.placeId == place.placeId })
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
        // TODO(BE 죠): 코스 구성용 추천 type enum 확정값으로 교체.
        private const val RECOMMENDATION_TYPE_COURSE = "COURSE"
        private const val EMPTY_MESSAGE = "추천할 수 있는 장소가 없어요."
    }
}
