package com.umc.todait.feature.home

import androidx.lifecycle.viewModelScope
import com.umc.todait.R
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.location.LocationProvider
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
 * "취향 기반 추천 장소" 섹션은 GET /api/recommended-places 로 조회한다.
 * 위치 권한이 있으면 현재 좌표를 함께 보내 거리 기반 정렬(500m 이내 "가까워요")을 적용하고,
 * 없으면(권한 거부/조회 실패) 좌표 없이 요청해 지역 균형 정렬로 대체된다 — 둘 다 정상 응답이라 별도 에러 처리 불필요.
 * "오늘의 추천 코스" 섹션은 대응하는 추천 코스 API가 아직 없어 더미 데이터로 채운다(DUMMY_COURSES).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val locationProvider: LocationProvider,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(courses = DUMMY_COURSES))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecommendedPlaces()
    }

    fun loadRecommendedPlaces() {
        _uiState.update { it.copy(placesState = HomePlacesState.Loading) }
        viewModelScope.launch {
            val coordinate = locationProvider.getCurrentLocation()
            val result = homeRepository.getRecommendedPlaces(
                size = HOME_PLACES_SIZE,
                latitude = coordinate?.latitude,
                longitude = coordinate?.longitude,
            )
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
        const val HOME_PLACES_SIZE = 2
        const val EMPTY_PLACES_MESSAGE = "아직 추천할 장소가 없어요."

        // TODO(BE): "오늘의 추천 코스" 조회 API 확정되면 실제 연동으로 교체.
        val DUMMY_COURSES = listOf(
            CourseCardUiModel(
                courseId = 1L,
                title = "연남 데이트 코스",
                hashtags = listOf("#낭만", "#분위기"),
                gradientStart = CourseRomanticGradientStart,
                gradientEnd = CourseRomanticGradientEnd,
                imageRes = R.drawable.img_home_course_yeonnam,
                decorationRes = R.drawable.ic_home_course_flower,
            ),
            CourseCardUiModel(
                courseId = 2L,
                title = "홍대 데이트 코스",
                hashtags = listOf("#힙한", "#칵테일"),
                gradientStart = CourseHipGradientStart,
                gradientEnd = CourseHipGradientEnd,
                imageRes = R.drawable.img_home_course_hongdae,
                decorationRes = R.drawable.ic_home_deco_1,
            ),
        )
    }
}
