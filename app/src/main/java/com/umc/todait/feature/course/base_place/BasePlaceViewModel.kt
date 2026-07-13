package com.umc.todait.feature.course.base_place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.location.Coordinate
import com.umc.todait.core.location.LocationProvider
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.data.repository.RecommendationRepository
import com.umc.todait.feature.course.data.repository.SearchRepository
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
 * 기준 장소 설정 화면(와이어프레임 1.1) + 확인 모달(1.2)의 상태를 관리한다.
 *
 * - 진입 시 화면의 위치 권한 플로우가 끝나면([onLocationPermissionResult])
 *   현재 위치를 확보한 뒤 "지금 내 주변 핫플" 추천 목록을 불러온다.
 * - 검색어 입력 후 검색 시 장소명 검색 결과를 보여준다.
 * - 카드 우측 상단 선택 버튼 → 기준 장소 단일 선택([onSelectPlace]).
 * - 헤더 확인(체크) 버튼([onConfirmClick]) → 확인 모달 → [확인] 시 지원 지역 검증 후 코스 구성하기로 이동.
 *   (카드 본문 탭은 이 ViewModel 이 아니라 화면에서 장소 상세 화면 진입으로 처리한다.)
 */
@HiltViewModel
class BasePlaceViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val recommendationRepository: RecommendationRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BasePlaceUiState())
    val uiState: StateFlow<BasePlaceUiState> = _uiState.asStateFlow()

    private val _effect = Channel<BasePlaceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // 요청 시점 사용자 위치. 권한 거부/조회 실패 시 null 로 두고 위·경도 없이 조회한다.
    private var currentLocation: Coordinate? = null

    // 최초 로드가 이미 시작됐는지. 화면 재구성(회전 등)으로 권한 콜백이 다시 와도 중복 로드하지 않는다.
    private var initialLoadStarted = false

    /**
     * 화면의 위치 권한 요청 플로우가 끝나면 호출된다.
     * 허용 시 현재 위치를 1회 조회해 보관하고, 거부 시 위치 없이 추천 목록을 불러온다.
     */
    fun onLocationPermissionResult(granted: Boolean) {
        if (initialLoadStarted) return
        initialLoadStarted = true
        viewModelScope.launch {
            if (granted) {
                currentLocation = locationProvider.getCurrentLocation()
            }
            loadNearbyHotPlaces()
        }
    }

    /** "지금 내 주변 핫플" 추천 목록 조회. 확보해 둔 현재 위치가 있으면 함께 전달한다. */
    fun loadNearbyHotPlaces() {
        _uiState.update { it.copy(listState = PlaceListState.Loading) }
        viewModelScope.launch {
            // TODO(BE 죠): 추천 type 값·기준 장소/코스 draft 파라미터 확정 필요.
            val result = recommendationRepository.getRecommendedPlaces(
                type = RECOMMENDATION_TYPE_NEARBY,
                latitude = currentLocation?.latitude,
                longitude = currentLocation?.longitude,
            )
            _uiState.update { state ->
                when (result) {
                    is ApiResult.Success -> {
                        val places = result.data.places.map { it.toUiModel() }
                        state.copy(
                            listState = if (places.isEmpty()) {
                                PlaceListState.Empty(EMPTY_NEARBY_MESSAGE)
                            } else {
                                PlaceListState.Success(places)
                            },
                        )
                    }

                    is ApiResult.Failure ->
                        state.copy(listState = PlaceListState.Error(result.toUiError().message))
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /** 검색어를 비우고 추천 목록으로 되돌린다. */
    fun onClearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        loadNearbyHotPlaces()
    }

    /** 검색 실행(장소명 검색). */
    fun onSearch() {
        val keyword = _uiState.value.searchQuery.trim()
        if (keyword.isBlank()) return

        _uiState.update { it.copy(listState = PlaceListState.Loading) }
        viewModelScope.launch {
            val result = searchRepository.searchPlacesByName(keyword = keyword)
            _uiState.update { state ->
                when (result) {
                    is ApiResult.Success -> {
                        val places = result.data.places.map { it.toUiModel() }
                        state.copy(
                            listState = if (places.isEmpty()) {
                                PlaceListState.Empty(EMPTY_SEARCH_MESSAGE)
                            } else {
                                PlaceListState.Success(places)
                            },
                        )
                    }

                    is ApiResult.Failure ->
                        state.copy(listState = PlaceListState.Error(result.toUiError().message))
                }
            }
        }
    }

    /**
     * 카드 우측 상단 선택 버튼 → 기준 장소 단일 선택.
     * 이미 선택된 장소를 다시 누르면 선택을 해제한다.
     */
    fun onSelectPlace(place: PlaceUiModel) {
        _uiState.update { state ->
            val next = if (state.selectedPlace?.placeId == place.placeId) null else place
            state.copy(selectedPlace = next)
        }
    }

    /** 헤더 확인(체크) 버튼 → 선택된 장소로 확인 모달 노출. 선택이 없으면 무시. */
    fun onConfirmClick() {
        val selected = _uiState.value.selectedPlace ?: return
        _uiState.update { it.copy(pendingPlace = selected, confirmError = null) }
    }

    /** 확인 모달 [취소] 또는 dismiss. */
    fun onDismissConfirm() {
        _uiState.update { it.copy(pendingPlace = null, confirmError = null) }
    }

    /**
     * 확인 모달 [확인]. 지원 지역/좌표를 검증하고 통과하면 코스 구성하기로 이동한다.
     * (임시 코스 세션 저장·기준 장소 주변 추천 조회는 세션 API 연동 시 처리 — 아래 TODO)
     */
    fun onConfirmSelection() {
        val place = _uiState.value.pendingPlace ?: return
        when {
            !place.hasCoordinate() ->
                _uiState.update { it.copy(confirmError = ERROR_NO_COORDINATE) }

            // 추천 장소는 지원 지역 내에서만 내려오므로 areaName 이 비어 있으면(추천 출처) 검증을 건너뛴다.
            place.areaName.isNotBlank() && place.areaName !in SUPPORTED_AREAS ->
                _uiState.update { it.copy(confirmError = ERROR_UNSUPPORTED_AREA) }

            else -> {
                // TODO(BE 죠): 기준 장소(위도/경도/카테고리/지역)를 임시 코스 세션에 저장 후 이동.
                _uiState.update { it.copy(pendingPlace = null, confirmError = null) }
                viewModelScope.launch { _effect.send(BasePlaceEffect.NavigateToCompose) }
            }
        }
    }

    private fun PlaceUiModel.hasCoordinate(): Boolean = latitude != 0.0 || longitude != 0.0

    companion object {
        // TODO(BE 죠): 추천 API 의 type enum 확정값으로 교체.
        private const val RECOMMENDATION_TYPE_NEARBY = "NEARBY"

        // 지원 지역(와이어프레임 1.3). 명세의 지역명(홍대/연남/성수) 기준.
        private val SUPPORTED_AREAS = setOf("홍대", "연남", "성수")

        // 명세 문구(와이어프레임 1.2 예외 상황). core/network 의 UiError.kt 와 동일하게 로직 레이어 상수로 둔다.
        private const val ERROR_UNSUPPORTED_AREA = "현재는 홍대, 연남, 성수 지역만 코스 생성을 지원해요."
        private const val ERROR_NO_COORDINATE = "장소 정보를 불러올 수 없습니다. 다른 장소를 선택해주세요."
        private const val EMPTY_NEARBY_MESSAGE = "지금 추천할 수 있는 주변 핫플이 없어요."
        // 검색 결과 없음(와이어프레임: 검색 결과 없음 화면). 디자인상 검색어를 포함하지 않는 일반 문구.
        private const val EMPTY_SEARCH_MESSAGE = "검색 결과가 없어요"
    }
}

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface BasePlaceEffect {
    data object NavigateToCompose : BasePlaceEffect
}
