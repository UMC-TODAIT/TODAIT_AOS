package com.umc.todait.feature.course.base_place

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.data.repository.CourseDraftRepository
import com.umc.todait.feature.course.data.repository.RecommendationRepository
import com.umc.todait.feature.course.data.repository.SearchRepository
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
 * 기준 장소 설정 화면(와이어프레임 1.1) + 확인 모달(1.2)의 상태를 관리한다.
 *
 * - 진입 시 "지금 내 주변 핫플" 추천 목록을 불러온다.
 * - 검색어 입력 후 검색 시 장소명 검색 결과를 보여준다.
 * - 카드 우상단 '+' 로 기준 장소를 선택하고, 헤더 체크 → 확정 알럿 → [확인] 시
 *   지원 지역 검증 후 코스 구성하기로 이동. (카드 본문 탭은 장소 상세로 진입 — 화면 레이어에서 처리)
 */
@HiltViewModel
class BasePlaceViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val recommendationRepository: RecommendationRepository,
    private val courseDraftRepository: CourseDraftRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // 분위기·음식 선택 화면에서 발급되어 코스 생성 플로우 전체가 공유하는 임시 코스 핸들.
    private val courseDraftId: Long = checkNotNull(savedStateHandle[Screen.BasePlace.ARG_COURSE_DRAFT_ID]) {
        "courseDraftId 인자가 필요합니다."
    }

    private val _uiState = MutableStateFlow(BasePlaceUiState())
    val uiState: StateFlow<BasePlaceUiState> = _uiState.asStateFlow()

    private val _effect = Channel<BasePlaceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadNearbyHotPlaces()
    }

    /**
     * "지금 내 주변 핫플" 추천 목록 조회
     * (GET /api/course-drafts/{courseDraftId}/hot-places).
     *
     * 위치 권한 플로우는 이번 범위 밖이라 위·경도를 전달하지 않는다. 이 경우 서버가
     * locationAvailable = false 로 취향·지역 균형 기반 추천을 내려준다.
     */
    fun loadNearbyHotPlaces() {
        _uiState.update { it.copy(listState = PlaceListState.Loading) }
        viewModelScope.launch {
            val result = recommendationRepository.getHotPlaces(courseDraftId = courseDraftId)
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

    /**
     * 검색 실행(GET /api/places/search?query=).
     * 명세상 공백 제거 후 2자 미만이면 서버가 PLACE_SEARCH400 을 주므로 호출 전에 걸러낸다.
     */
    fun onSearch() {
        val query = _uiState.value.searchQuery.trim()
        if (query.length < MIN_SEARCH_QUERY_LENGTH) {
            _uiState.update { it.copy(listState = PlaceListState.Empty(SHORT_QUERY_MESSAGE)) }
            return
        }

        _uiState.update { it.copy(listState = PlaceListState.Loading) }
        viewModelScope.launch {
            val result = searchRepository.searchPlaces(query = query)
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

    /** 카드 우상단 '+' → 기준 장소 선택/해제(토글). 단일 선택. */
    fun onSelectPlace(place: PlaceUiModel) {
        _uiState.update { state ->
            val next = if (state.selectedPlace?.key == place.key) null else place
            state.copy(selectedPlace = next)
        }
    }

    /**
     * 헤더 체크(확정) 버튼.
     * - 선택된 장소가 없으면 시스템알럿1(선택 요청)을 띄운다.
     * - 있으면 시스템알럿2(확정 확인)를 띄운다.
     */
    fun onConfirmClick() {
        _uiState.update { state ->
            val selected = state.selectedPlace
            val alert = if (selected == null) {
                BasePlaceAlert.SelectRequired
            } else {
                BasePlaceAlert.Confirm(selected)
            }
            state.copy(alert = alert, confirmError = null)
        }
    }

    /** 알럿 [취소] 또는 dismiss. */
    fun onDismissAlert() {
        _uiState.update { it.copy(alert = null, confirmError = null) }
    }

    /**
     * 확정 알럿 [확인]. 지원 지역/좌표를 검증한 뒤 기준 장소를 임시 코스에 저장하고
     * (PATCH /api/course-drafts/{courseDraftId}/base-place) 코스 구성하기로 이동한다.
     *
     * 저장에 성공하면 임시 코스 상태가 BASE_PLACE_SELECTING → PLACE_SELECTING 으로 넘어가,
     * 다음 화면의 카테고리별 추천 조회가 가능해진다.
     */
    fun onConfirmSelection() {
        val state = _uiState.value
        val place = state.selectedPlace ?: return
        if (state.isConfirming) return

        when {
            !place.hasCoordinate() -> {
                _uiState.update { it.copy(confirmError = ERROR_NO_COORDINATE) }
                return
            }

            // 추천 장소는 지원 지역 내에서만 내려오므로 areaName 이 비어 있으면(추천 출처) 검증을 건너뛴다.
            place.areaName.isNotBlank() && place.areaName !in SUPPORTED_AREAS -> {
                _uiState.update { it.copy(confirmError = ERROR_UNSUPPORTED_AREA) }
                return
            }
        }

        _uiState.update { it.copy(isConfirming = true, confirmError = null) }
        viewModelScope.launch {
            val result = if (place.placeId != null) {
                // 내부 DB 에 이미 있는 장소(주변 핫플·운영자 등록·등록된 카카오 장소).
                courseDraftRepository.setBasePlace(courseDraftId = courseDraftId, placeId = place.placeId)
            } else {
                // 내부 미등록 카카오 검색 장소 → externalPlace 로 보내면 서버가 place 를 만들어 준다.
                val externalPlace = place.toExternalPlaceDto()
                if (externalPlace == null) {
                    _uiState.update { it.copy(isConfirming = false, confirmError = ERROR_NO_PLACE_INFO) }
                    return@launch
                }
                courseDraftRepository.setBasePlaceFromExternal(
                    courseDraftId = courseDraftId,
                    externalPlace = externalPlace,
                )
            }

            when (result) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(alert = null, confirmError = null, isConfirming = false) }
                    _effect.send(
                        BasePlaceEffect.NavigateToCompose(
                            courseDraftId = courseDraftId,
                            // 카카오 검색 장소를 새로 등록한 경우에도 서버가 만든 내부 placeId 가 온다.
                            basePlaceId = result.data.basePlace.placeId,
                        ),
                    )
                }

                is ApiResult.Failure ->
                    _uiState.update {
                        it.copy(isConfirming = false, confirmError = result.toUiError().message)
                    }
            }
        }
    }

    private fun PlaceUiModel.hasCoordinate(): Boolean = latitude != 0.0 || longitude != 0.0

    companion object {
        // 명세: 검색어는 공백 제거 후 2자 이상.
        private const val MIN_SEARCH_QUERY_LENGTH = 2

        // 지원 지역(와이어프레임 1.3). 명세의 지역명(홍대/연남/성수) 기준.
        private val SUPPORTED_AREAS = setOf("홍대", "연남", "성수")

        // 명세 문구(와이어프레임 1.2 예외 상황). core/network 의 UiError.kt 와 동일하게 로직 레이어 상수로 둔다.
        private const val ERROR_UNSUPPORTED_AREA = "현재는 홍대, 연남, 성수 지역만 코스 생성을 지원해요."
        private const val ERROR_NO_COORDINATE = "장소 정보를 불러올 수 없습니다. 다른 장소를 선택해주세요."
        // 미등록 장소인데 externalPlace 필수값(외부 ID·지역·카테고리)이 비어 서버로 보낼 수 없는 경우.
        private const val ERROR_NO_PLACE_INFO = "이 장소는 기준 장소로 설정할 수 없어요. 다른 장소를 선택해주세요."
        private const val EMPTY_NEARBY_MESSAGE = "지금 추천할 수 있는 주변 핫플이 없어요."
        // 검색 결과 없음(와이어프레임: 검색 결과 없음 화면). 디자인상 검색어를 포함하지 않는 일반 문구.
        private const val EMPTY_SEARCH_MESSAGE = "검색 결과가 없어요"
        private const val SHORT_QUERY_MESSAGE = "검색어를 2자 이상 입력해주세요."
    }
}

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface BasePlaceEffect {
    /**
     * 기준 장소 저장 성공 → 코스 구성하기로 이동.
     *
     * 코스 구성 이후 단계(추천 조회·순서 설정·저장)가 모두 [courseDraftId] 를 경로 변수로 쓰므로
     * 여기서 확정된 임시 코스 핸들과 기준 장소 id 를 함께 넘긴다.
     */
    data class NavigateToCompose(
        val courseDraftId: Long,
        val basePlaceId: Long,
    ) : BasePlaceEffect
}
