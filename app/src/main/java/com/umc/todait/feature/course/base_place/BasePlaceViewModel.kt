package com.umc.todait.feature.course.base_place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.data.repository.CourseDraftRepository
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(BasePlaceUiState())
    val uiState: StateFlow<BasePlaceUiState> = _uiState.asStateFlow()

    private val _effect = Channel<BasePlaceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // 주변 핫플 조회에 필요한 임시 코스 핸들.
    private var courseDraftId: Long? = null

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
            val draftId = ensureCourseDraftId()
            if (draftId == null) {
                _uiState.update { it.copy(listState = PlaceListState.Error(ERROR_DRAFT_REQUIRED)) }
                return@launch
            }

            val result = recommendationRepository.getHotPlaces(courseDraftId = draftId)
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

    /**
     * 임시 코스 핸들 확보.
     *
     * TODO(2차): 임시 코스는 원래 코스 생성 진입(분위기 선택) 시점에 만들어 플로우 전체가 공유해야 한다.
     *  분위기/음식 저장 API 연동 전까지는 이 화면에서 발급해 hot-places 호출 핸들을 확보한다.
     */
    private suspend fun ensureCourseDraftId(): Long? {
        courseDraftId?.let { return it }
        return when (val result = courseDraftRepository.createCourseDraft()) {
            is ApiResult.Success -> result.data.courseDraftId.also { courseDraftId = it }
            is ApiResult.Failure -> null
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
     * 확정 알럿 [확인]. 지원 지역/좌표를 검증하고 통과하면 코스 구성하기로 이동한다.
     * (임시 코스 세션 저장·기준 장소 주변 추천 조회는 세션 API 연동 시 처리 — 아래 TODO)
     */
    fun onConfirmSelection() {
        val place = _uiState.value.selectedPlace ?: return
        when {
            !place.hasCoordinate() ->
                _uiState.update { it.copy(confirmError = ERROR_NO_COORDINATE) }

            // 추천 장소는 지원 지역 내에서만 내려오므로 areaName 이 비어 있으면(추천 출처) 검증을 건너뛴다.
            place.areaName.isNotBlank() && place.areaName !in SUPPORTED_AREAS ->
                _uiState.update { it.copy(confirmError = ERROR_UNSUPPORTED_AREA) }

            else -> {
                // TODO(BE 죠): 기준 장소(위도/경도/카테고리/지역)를 임시 코스 세션에 저장 후 이동.
                _uiState.update { it.copy(alert = null, confirmError = null) }
                viewModelScope.launch { _effect.send(BasePlaceEffect.NavigateToCompose) }
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
        private const val ERROR_DRAFT_REQUIRED = "코스 생성 정보를 불러오지 못했어요. 다시 시도해주세요."
        private const val EMPTY_NEARBY_MESSAGE = "지금 추천할 수 있는 주변 핫플이 없어요."
        // 검색 결과 없음(와이어프레임: 검색 결과 없음 화면). 디자인상 검색어를 포함하지 않는 일반 문구.
        private const val EMPTY_SEARCH_MESSAGE = "검색 결과가 없어요"
        private const val SHORT_QUERY_MESSAGE = "검색어를 2자 이상 입력해주세요."
    }
}

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface BasePlaceEffect {
    data object NavigateToCompose : BasePlaceEffect
}
