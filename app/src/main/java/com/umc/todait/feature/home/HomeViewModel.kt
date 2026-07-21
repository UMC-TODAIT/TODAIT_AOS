package com.umc.todait.feature.home

import androidx.lifecycle.viewModelScope
import com.umc.todait.R
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.home.data.repository.HomeRepository
import com.umc.todait.ui.theme.CourseHipGradientEnd
import com.umc.todait.ui.theme.CourseHipGradientStart
import com.umc.todait.ui.theme.CourseRomanticGradientEnd
import com.umc.todait.ui.theme.CourseRomanticGradientStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 홈 화면의 상태를 관리한다.
 *
 * "취향 기반 추천 장소" 섹션은 GET /api/recommendations/places?type=HOME_PLACE 로 조회한다.
 * "오늘의 추천 코스" 섹션은 대응하는 추천 코스 API가 아직 없어 더미 데이터로 채운다(DUMMY_COURSES).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(courses = DUMMY_COURSES))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecommendedPlaces()
    }

    fun loadRecommendedPlaces() {
        _uiState.update { it.copy(placesState = HomePlacesState.Loading) }
        viewModelScope.launch {
            val result = homeRepository.getRecommendedPlaces(type = RECOMMENDATION_TYPE_HOME_PLACE)
            _uiState.update { state ->
                when (result) {
                    is ApiResult.Success -> {
                        val places = result.data.places.map { it.toUiModel() }
                        state.copy(
                            placesState = if (places.isEmpty()) {
                                HomePlacesState.Empty(EMPTY_PLACES_MESSAGE)
                            } else {
                                HomePlacesState.Success(places)
                            },
                        )
                    }

                    is ApiResult.Failure ->
                        state.copy(placesState = HomePlacesState.Error(result.toUiError().message))
                }
            }
        }
    }

    private companion object {
        // TODO(BE 죠/멍이): 추천 장소 type enum 확정값(HOME_PLACE)으로 교체 확인 필요.
        const val RECOMMENDATION_TYPE_HOME_PLACE = "HOME_PLACE"
        const val EMPTY_PLACES_MESSAGE = "아직 추천할 장소가 없어요."

        // TODO(BE): "오늘의 추천 코스" 조회 API 확정되면 실제 연동으로 교체.
        val DUMMY_COURSES = listOf(
            CourseCardUiModel(
                courseId = 1L,
                title = "연남 데이트 코스",
                hashtags = listOf("#낭만", "#분위기"),
                gradientStart = CourseRomanticGradientStart,
                gradientEnd = CourseRomanticGradientEnd,
                decorationRes = R.drawable.ic_home_deco_2,
            ),
            CourseCardUiModel(
                courseId = 2L,
                title = "홍대 데이트 코스",
                hashtags = listOf("#힙한", "#칵테일"),
                gradientStart = CourseHipGradientStart,
                gradientEnd = CourseHipGradientEnd,
                decorationRes = R.drawable.ic_home_deco_1,
            ),
        )
    }
}
