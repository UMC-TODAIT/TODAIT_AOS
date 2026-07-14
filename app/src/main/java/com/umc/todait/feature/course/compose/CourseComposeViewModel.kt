package com.umc.todait.feature.course.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.base_place.PlaceUiModel
import com.umc.todait.feature.course.base_place.toUiModel
import com.umc.todait.feature.course.data.repository.RecommendationRepository
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
 * 코스 구성하기 화면(#26)의 상태를 관리한다.
 *
 * 진입 시 선택된 카테고리(기본 카페) 기준으로 기준 장소 주변 추천 장소를 불러오고,
 * 카드 '+' 로 코스에 담는다. 헤더 ✓ 확정 시 [CourseComposeEffect.NavigateNext] 를 방출한다.
 *
 * ⚠️ 기준 장소/임시 코스 세션(courseDraftId·basePlaceId)은 세션 API 연동 시 채운다. (아래 TODO)
 */
@HiltViewModel
class CourseComposeViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseComposeUiState())
    val uiState: StateFlow<CourseComposeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CourseComposeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadRecommendations()
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
    fun onSelectCategory(category: CourseCategory) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
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

    /** 헤더 ✓ 확정 → 다음 단계로 이동. 담은 장소가 있을 때만 동작. */
    fun onConfirm() {
        if (!_uiState.value.canConfirm) return
        // TODO(BE 죠): 구성한 코스(담은 장소 순서)를 임시 코스 세션에 저장 후 이동.
        viewModelScope.launch { _effect.send(CourseComposeEffect.NavigateNext) }
    }

    companion object {
        // TODO(BE 죠): 코스 구성용 추천 type enum 확정값으로 교체.
        private const val RECOMMENDATION_TYPE_COURSE = "COURSE"
        private const val EMPTY_MESSAGE = "추천할 수 있는 장소가 없어요."
    }
}
