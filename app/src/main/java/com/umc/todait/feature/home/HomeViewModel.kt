package com.umc.todait.feature.home

import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.location.LocationProvider
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.home.data.repository.HomeRepository
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
 * "오늘의 추천 코스" 섹션은 GET /api/recommended-courses(page=0, size=3) 로 조회한다.
 * 서버가 날짜 기준으로 홍대/연남/성수 코스를 순환 반환하므로 프론트는 페이지네이션 없이 첫 페이지만 쓴다.
 * "취향 기반 추천 장소" 섹션은 GET /api/recommended-places 로 조회한다.
 * 위치 권한이 있으면 현재 좌표를 함께 보내 거리 기반 정렬(500m 이내 "가까워요")을 적용하고,
 * 없으면(권한 거부/조회 실패) 좌표 없이 요청해 지역 균형 정렬로 대체된다 — 둘 다 정상 응답이라 별도 에러 처리 불필요.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val locationProvider: LocationProvider,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecommendedCourses()
        loadRecommendedPlaces()
    }

    fun loadRecommendedCourses() {
        _uiState.update { it.copy(coursesState = CoursesState.Loading) }
        viewModelScope.launch {
            val result = homeRepository.getRecommendedCourses(size = HOME_COURSES_SIZE)
            _uiState.update { state ->
                when (result) {
                    is ApiResult.Success -> {
                        val courses = result.data.courses.map { it.toUiModel() }
                        state.copy(
                            coursesState = if (courses.isEmpty()) {
                                CoursesState.Empty(EMPTY_COURSES_MESSAGE)
                            } else {
                                CoursesState.Success(courses)
                            },
                        )
                    }

                    is ApiResult.Failure ->
                        state.copy(coursesState = CoursesState.Error(result.toUiError().message))
                }
            }
        }
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
        const val HOME_COURSES_SIZE = 3
        const val EMPTY_COURSES_MESSAGE = "아직 추천할 코스가 없어요."
        const val HOME_PLACES_SIZE = 2
        const val EMPTY_PLACES_MESSAGE = "아직 추천할 장소가 없어요."
    }
}
