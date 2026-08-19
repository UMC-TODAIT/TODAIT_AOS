package com.umc.todait.feature.course.base_place

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.data.dto.CourseDraftStatus
import com.umc.todait.feature.course.data.dto.SearchEmptyReason
import com.umc.todait.feature.course.data.repository.CourseDraftRepository
import com.umc.todait.feature.course.data.repository.RecommendationRepository
import com.umc.todait.feature.course.data.repository.SearchRepository
import com.umc.todait.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
 * - 카드 탭으로 기준 장소를 선택하고, 헤더 체크 → 확정 알럿 → [확인] 시
 *   지원 지역 검증 후 코스 구성하기로 이동. (카드 롱프레스는 장소 상세로 진입 — 화면 레이어에서 처리)
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

    // --- 검색 결과 커서 페이지네이션 상태 (명세 "프론트 연동 요약") ---
    // 다음 요청에 넘길 cursor(직전 응답의 nextCursor). null 이면 첫 페이지이거나 더 볼 페이지가 없다는 뜻.
    private var searchCursor: Int? = null
    // 지금까지 누적한 검색 결과. 페이지를 이어 붙인 목록을 그대로 화면에 그린다.
    private var searchedPlaces: List<PlaceUiModel> = emptyList()
    // 누적 결과가 어떤 검색어의 것인지. 추가 조회 시 같은 검색어로만 이어 붙인다.
    private var searchedQuery: String = ""
    // 페이지 간 중복 제거 기준(명세: externalPlaceId).
    private val seenExternalIds = mutableSetOf<String>()
    // 진행 중인 검색 코루틴. 새 검색이 시작되면 이전 요청 결과를 버린다.
    private var searchJob: Job? = null

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
        resetSearchPaging()
        _uiState.update { it.copy(searchQuery = "", canLoadMoreSearch = false, isLoadingMoreSearch = false) }
        loadNearbyHotPlaces()
    }

    /**
     * 검색 실행(GET /api/places/search?query=&cursor=&size=). 항상 첫 페이지부터 다시 조회한다.
     * 명세상 공백 제거 후 2자 미만이면 서버가 PLACE_SEARCH400 을 주므로 호출 전에 걸러낸다.
     */
    fun onSearch() {
        val query = _uiState.value.searchQuery.trim()
        if (query.length < MIN_SEARCH_QUERY_LENGTH) {
            resetSearchPaging()
            _uiState.update {
                it.copy(
                    listState = PlaceListState.Empty(SHORT_QUERY_MESSAGE),
                    canLoadMoreSearch = false,
                    isLoadingMoreSearch = false,
                )
            }
            return
        }

        loadSearchPage(query = query, isFirstPage = true)
    }

    /**
     * 검색 결과 목록을 끝까지 스크롤했을 때 다음 페이지를 이어서 조회한다.
     * 남은 페이지가 없거나 이미 조회 중이면 아무것도 하지 않는다.
     */
    fun onLoadMoreSearchResults() {
        val state = _uiState.value
        if (!state.canLoadMoreSearch || state.isLoadingMoreSearch) return
        val query = searchedQuery.takeIf { it.isNotBlank() } ?: return
        loadSearchPage(query = query, isFirstPage = false)
    }

    /**
     * 검색 한 페이지(또는 필요한 만큼 연속된 페이지)를 조회해 결과를 누적한다.
     *
     * 명세 "프론트 연동 요약" 그대로 동작한다.
     * - 첫 요청은 cursor 없이(=1) 보내고, 이후에는 직전 응답의 nextCursor 를 cursor 로 넘긴다.
     * - hasNext=false 이거나 nextCursor=null 이면 추가 조회를 멈춘다.
     * - 페이지 간 중복 장소는 externalPlaceId 기준으로 제거한다.
     * - 서버가 지원 지역 밖 장소를 걸러내는 탓에 **places 가 비어 있는데 hasNext=true 인 페이지**가
     *   섞일 수 있다. 이때 멈추면 결과가 있는데도 "검색 결과가 없어요"가 뜨므로,
     *   새 장소를 한 건이라도 얻을 때까지 [MAX_PAGES_PER_LOAD] 페이지까지 이어서 조회한다.
     */
    private fun loadSearchPage(query: String, isFirstPage: Boolean) {
        if (isFirstPage) {
            resetSearchPaging()
            searchedQuery = query
            _uiState.update {
                it.copy(
                    listState = PlaceListState.Loading,
                    canLoadMoreSearch = false,
                    isLoadingMoreSearch = false,
                )
            }
        } else {
            _uiState.update { it.copy(isLoadingMoreSearch = true) }
        }

        // 이전 검색이 아직 돌고 있으면 결과를 버린다(검색어가 바뀌면 누적분도 cursor 도 재사용하지 않는다).
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val sizeBefore = searchedPlaces.size
            var failure: ApiResult.Failure? = null
            var emptyReason: String? = null
            var pages = 0

            while (pages < MAX_PAGES_PER_LOAD) {
                val result = searchRepository.searchPlaces(query = query, cursor = searchCursor)
                pages++
                val data = when (result) {
                    is ApiResult.Failure -> {
                        failure = result
                        break
                    }

                    is ApiResult.Success -> result.data
                }
                emptyReason = data.emptyReason
                // hasNext=true 라도 nextCursor 가 없으면 더 보낼 커서가 없다 → 종료 조건으로 본다.
                searchCursor = data.nextCursor.takeIf { data.hasNext }

                val newPlaces = data.places
                    .filter { seenExternalIds.add(it.externalPlaceId) }
                    .map { it.toUiModel() }
                searchedPlaces = searchedPlaces + newPlaces

                // 새로 담은 장소가 있으면 여기서 멈추고 화면에 그린다. 없으면 다음 커서로 이어 조회.
                if (searchedPlaces.size > sizeBefore || searchCursor == null) break
            }

            val canLoadMore = searchCursor != null
            val places = searchedPlaces
            val errorMessage = failure?.toUiError()?.message
            val listState = when {
                // 누적된 결과가 하나도 없는 실패만 에러 화면으로 바꾼다. 이미 그린 결과가 있으면 목록을 유지한다.
                places.isEmpty() && errorMessage != null -> PlaceListState.Error(errorMessage)
                places.isEmpty() -> emptyStateFor(query = query, emptyReason = emptyReason)
                else -> PlaceListState.Success(places)
            }
            _uiState.update { state ->
                state.copy(
                    listState = listState,
                    // 추가 페이지가 실패해도 cursor 는 그대로 두어, 다시 끝까지 스크롤하면 재시도할 수 있게 한다.
                    canLoadMoreSearch = canLoadMore,
                    isLoadingMoreSearch = false,
                )
            }
        }
    }

    /** 검색어가 바뀌었거나 검색을 그만둘 때 누적 결과·커서를 모두 버린다. */
    private fun resetSearchPaging() {
        searchJob?.cancel()
        searchJob = null
        searchCursor = null
        searchedPlaces = emptyList()
        seenExternalIds.clear()
        searchedQuery = ""
    }

    /**
     * 최종적으로 결과가 없을 때의 빈 화면 문구.
     * 서버가 사유(emptyReason)를 주면 그대로 따르고, 없으면 검색어로 추정한다.
     */
    private fun emptyStateFor(query: String, emptyReason: String?): PlaceListState.Empty =
        if (emptyReason == SearchEmptyReason.OUTSIDE_SUPPORTED_AREA || isUnsupportedAreaQuery(query)) {
            PlaceListState.Empty(UNSUPPORTED_AREA_TITLE, UNSUPPORTED_AREA_DESC)
        } else {
            PlaceListState.Empty(EMPTY_SEARCH_TITLE, EMPTY_SEARCH_DESC)
        }

    /** 카드 탭 → 기준 장소 선택/해제(토글). 단일 선택. */
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

    /**
     * 이전 버튼(`<`) → 단계 이동 API 호출 후 음식 선택 화면으로 돌아간다.
     *
     * 단순 화면 이동이라 기준 장소를 지우지도, 알림을 띄우지도 않는다. 단계 이동 호출이
     * 실패해도 화면은 넘긴다 — 사용자에게는 뒤로 가기일 뿐이다.
     */
    fun onBackClick() {
        viewModelScope.launch {
            courseDraftRepository.moveToStatus(courseDraftId, CourseDraftStatus.FOOD_SELECTING)
            _effect.send(BasePlaceEffect.NavigateBack)
        }
    }

    private fun PlaceUiModel.hasCoordinate(): Boolean = latitude != 0.0 || longitude != 0.0

    companion object {
        // 명세: 검색어는 공백 제거 후 2자 이상.
        private const val MIN_SEARCH_QUERY_LENGTH = 2

        // 한 번의 조회에서 이어서 볼 최대 페이지 수.
        // 지원 지역 밖 장소만 걸린 빈 페이지(places=[], hasNext=true)를 건너뛰기 위한 상한선으로,
        // 서버 cursor 범위(1~45)를 무한정 훑지 않도록 막는다.
        private const val MAX_PAGES_PER_LOAD = 5

        // 지원 지역(와이어프레임 1.3). 명세의 지역명(홍대/연남/성수) 기준.
        private val SUPPORTED_AREAS = setOf("홍대", "연남", "성수")

        private fun isUnsupportedAreaQuery(query: String): Boolean {
            val unsupportedKeywords = listOf(
                "강남", "이태원", "신촌", "잠실", "여의도", "종로", "명동", "부산",
                "제주", "해운대", "판교", "분당", "수원", "인천", "대구", "대전", "광주"
            )
            return unsupportedKeywords.any { query.contains(it) }
        }

        // 명세 문구(와이어프레임 1.2 예외 상황). core/network 의 UiError.kt 와 동일하게 로직 레이어 상수로 둔다.
        private const val ERROR_UNSUPPORTED_AREA = "현재는 홍대, 연남, 성수 지역만 코스 생성을 지원해요."
        private const val ERROR_NO_COORDINATE = "장소 정보를 불러올 수 없습니다. 다른 장소를 선택해주세요."
        // 미등록 장소인데 externalPlace 필수값(외부 ID·지역·카테고리)이 비어 서버로 보낼 수 없는 경우.
        private const val ERROR_NO_PLACE_INFO = "이 장소는 기준 장소로 설정할 수 없어요. 다른 장소를 선택해주세요."
        private const val EMPTY_NEARBY_MESSAGE = "지금 추천할 수 있는 주변 핫플이 없어요."
        // 검색 결과 없음 / 지원 지역 외 (Figma node 894:3358 / 3125:18099)
        private const val EMPTY_SEARCH_TITLE = "검색 결과가 없어요"
        private const val EMPTY_SEARCH_DESC = "다른 검색어로 다시 검색해보세요."
        private const val UNSUPPORTED_AREA_TITLE = "현재 지원 지역에 해당하는 장소가 없어요."
        private const val UNSUPPORTED_AREA_DESC = "투데잇은 현재 홍대, 연남, 성수 지역을 지원하고 있어요."
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

    /** 이전 버튼(`<`) → 단계 이동 API 를 부른 뒤 음식 선택 화면으로 돌아간다. */
    data object NavigateBack : BasePlaceEffect
}
