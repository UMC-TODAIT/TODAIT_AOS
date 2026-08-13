package com.umc.todait.feature.course.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.location.LocationProvider
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.base_place.PlaceUiModel
import com.umc.todait.feature.course.base_place.toUiModel
import com.umc.todait.feature.course.data.dto.CourseDraftPlaceDto
import com.umc.todait.feature.course.data.dto.CourseDraftStatus
import com.umc.todait.feature.course.data.repository.CourseDraftRepository
import com.umc.todait.feature.course.data.repository.PlaceCategoryRepository
import com.umc.todait.feature.course.data.repository.PlaceRepository
import com.umc.todait.feature.course.data.repository.RecommendationRepository
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
 * 코스 구성하기 플로우(#26)의 **공유** 상태를 관리한다(선택 화면 + 선택한 장소 화면 + 코스 저장 화면).
 * NavHost 의 course/compose 중첩 그래프에 스코프되어 세 화면이 같은 인스턴스를 쓴다.
 *
 * 임시 코스 핸들(courseDraftId)과 기준 장소(basePlaceId)는 기준 장소 설정 화면에서 확정된 뒤
 * 그래프 경로 변수로 넘어온다. 이후 단계는 모두 이 핸들 위에서 상태를 전이시킨다.
 *
 * - 진입: 기준 장소 상세 + 카테고리 탭 로드 → 카테고리별 추천 장소 조회
 * - [onAddPlace] (추천 카드 탭): POST .../places — 임시 코스에 장소를 담고 courseDraftPlaceId 를 받는다
 * - [onSelectionConfirmed] (장소 선택 ✓): PATCH .../ordering — PLACE_SELECTING → ORDERING
 * - [onOrderConfirmed] (순서 설정 ✓): PATCH .../places/order 로 최종 순서 저장 후 PATCH .../saving
 */
@HiltViewModel
class CourseComposeViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val placeCategoryRepository: PlaceCategoryRepository,
    private val courseDraftRepository: CourseDraftRepository,
    private val placeRepository: PlaceRepository,
    private val locationProvider: LocationProvider,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // 기준 장소 설정 화면에서 확정된 임시 코스 핸들. 코스 구성 그래프 전체가 공유한다.
    private val courseDraftId: Long = checkNotNull(savedStateHandle[Screen.CourseComposeGraph.ARG_COURSE_DRAFT_ID]) {
        "courseDraftId 인자가 필요합니다."
    }

    // 기준 장소의 내부 place id. 지도 1번 핀·경로 미리보기 첫 번째 장소로 쓴다.
    private val basePlaceId: Long = checkNotNull(savedStateHandle[Screen.CourseComposeGraph.ARG_BASE_PLACE_ID]) {
        "basePlaceId 인자가 필요합니다."
    }

    private val _uiState = MutableStateFlow(CourseComposeUiState(courseDraftId = courseDraftId))
    val uiState: StateFlow<CourseComposeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CourseComposeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // 둘 다 orderedPlaces 를 세우므로 순서를 고정한다. 담아둔 장소가 있으면 그 순서가 곧 코스 동선이고,
        // 없을 때만 기준 장소 하나로 시작한다. (동시에 돌리면 늦게 끝난 쪽이 순서를 덮어쓴다.)
        viewModelScope.launch {
            restoreDraftPlaces()
            if (_uiState.value.orderedPlaces.isEmpty()) loadBasePlace()
        }
        loadCurrentLocation()
        // 카테고리(탭)가 있어야 추천을 조회할 수 있어(placeCategoryCode 필요) 카테고리 로드 후 추천을 부른다.
        loadCategories()
    }

    /**
     * 임시 코스에 이미 담겨 있는 장소를 되살린다(GET /api/course-drafts/current).
     *
     * "이어서 하기"로 코스 구성 단계에 복귀하면 서버에는 기준 장소와 담은 장소가 남아 있지만
     * 화면 상태는 비어 있다. 응답의 visitOrder 순서를 그대로 코스 동선으로 삼고,
     * placeRole=BASE 인 장소를 기준 장소로 표시한다.
     *
     * 담은 장소가 하나도 없으면(기준 장소 직후) [loadBasePlace] 가 세운 상태를 그대로 둔다.
     * 조회에 실패해도 화면은 정상 동작한다 — 추천 목록에서 다시 담으면 된다.
     */
    private suspend fun restoreDraftPlaces() {
        val draft = (courseDraftRepository.getCurrentCourseDraft() as? ApiResult.Success)?.data ?: return
        if (draft.courseDraftId != courseDraftId) return

        val places = draft.places.orEmpty().sortedBy { it.visitOrder }
        if (places.isEmpty()) return

        val restored = places.map { it.toUiModel() }
        val baseKey = places.firstOrNull { it.placeRole == CourseDraftPlaceDto.PLACE_ROLE_BASE }
            ?.let { base -> restored.firstOrNull { it.placeId == base.placeId }?.key }

        _uiState.update { state ->
            state.copy(
                orderedPlaces = restored,
                // 기준 장소를 못 찾으면(응답에 BASE 가 없음) 그래프 인자로 받은 기준 장소를 그대로 쓴다.
                basePlaceKey = baseKey ?: state.basePlaceKey,
            )
        }
    }

    /**
     * 지도 "현재 위치" 마커용 좌표 1회 조회.
     * 권한이 없거나 조회에 실패하면 null 이 그대로 남아 마커만 빠진다(화면은 정상 동작).
     * 화면에서 위치 권한을 받아낸 뒤 다시 부를 수 있도록 public 이다.
     */
    fun loadCurrentLocation() {
        viewModelScope.launch {
            val coordinate = locationProvider.getCurrentLocation() ?: return@launch
            _uiState.update { it.copy(currentLocation = coordinate) }
        }
    }

    /**
     * 기준 장소 상세 조회(GET /api/places/{placeId}).
     *
     * 기준 장소 설정 API 응답에는 지도/경로 미리보기에 필요한 정보가 요약만 들어 있어,
     * 확정된 placeId 로 장소 카드 상세를 한 번 더 불러 화면 모델을 채운다.
     * 실패해도 추천 목록은 볼 수 있어야 하므로 목록 상태는 건드리지 않는다.
     */
    private suspend fun loadBasePlace() {
        when (val result = placeRepository.getPlaceDetail(basePlaceId)) {
            is ApiResult.Success -> _uiState.update { state ->
                val base = result.data.toBasePlaceUiModel()
                state.copy(
                    // 기준 장소도 코스 목록의 일원이다. 처음엔 맨 앞에 놓고, 이후 드래그로 옮길 수 있다.
                    orderedPlaces = listOf(base) + state.orderedPlaces.filterNot { it.key == base.key },
                    basePlaceKey = base.key,
                )
            }

            is ApiResult.Failure -> Unit
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
            val result = recommendationRepository.getRecommendedPlaces(
                courseDraftId = courseDraftId,
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
     * 추천 카드 탭 → 코스에 담기
     * (POST /api/course-drafts/{courseDraftId}/places).
     *
     * 서버가 임시 코스에 장소를 저장하고 courseDraftPlaceId 를 발급해 주므로, 응답을 받은 뒤에
     * 목록 끝에 추가한다(순서 변경 API 가 placeId 가 아니라 이 id 를 쓰기 때문).
     *
     * 이미 담은 장소는 요청 전에 걸러 중복 알럿을 띄우고, 요청 중인 장소는 [CourseComposeUiState.addingPlaceKeys]
     * 로 표시해 같은 카드의 중복 탭을 막는다. 최종 중복·기준 장소·최대 개수 검증은 서버가 한다.
     */
    fun onAddPlace(place: PlaceUiModel) {
        val state = _uiState.value
        if (state.selectedPlaces.any { it.key == place.key }) {
            _uiState.update { it.copy(alert = CourseComposeAlert.Duplicate) }
            return
        }
        if (place.key in state.addingPlaceKeys) return
        // 추천 카드는 운영자 검수를 마친 내부 장소라 placeId 가 항상 있다(외부 장소는 담을 수 없다).
        val placeId = place.placeId ?: run {
            _uiState.update { it.copy(alert = CourseComposeAlert.AddFailed(UNSUPPORTED_PLACE_MESSAGE)) }
            return
        }

        _uiState.update { it.copy(addingPlaceKeys = it.addingPlaceKeys + place.key) }
        viewModelScope.launch {
            val result = courseDraftRepository.addPlace(courseDraftId = courseDraftId, placeId = placeId)
            _uiState.update { current ->
                val next = current.copy(addingPlaceKeys = current.addingPlaceKeys - place.key)
                when (result) {
                    is ApiResult.Success -> {
                        // 요청 중에 다른 경로로 이미 담겼다면 그대로 둔다(중복 추가 방지).
                        if (next.selectedPlaces.any { it.key == place.key }) {
                            next
                        } else {
                            val added = place.copy(courseDraftPlaceId = result.data.addedPlace.courseDraftPlaceId)
                            next.copy(orderedPlaces = next.orderedPlaces + added)
                        }
                    }

                    // 서버 message 가 그대로 안내 문구가 된다(예: "이미 선택한 장소입니다.").
                    is ApiResult.Failure ->
                        next.copy(alert = CourseComposeAlert.AddFailed(result.toUiError().message))
                }
            }
        }
    }

    /**
     * 선택한 장소에서 빼기.
     *
     * ⚠️ "선택 장소 삭제"(DELETE .../places/{courseDraftPlaceId})가 아직 개발 전이라 화면에서만 뺀다.
     * 서버 임시 코스에는 장소가 남아 있어 순서/저장 단계에서 개수가 어긋날 수 있다.
     * 삭제 API 가 나오면 여기서 호출하고 실패 시 목록을 되돌린다.
     */
    fun onRemovePlace(place: PlaceUiModel) {
        _uiState.update { state ->
            // 기준 장소는 화면에서 뺄 수 없다(코스의 기준점이라 빼면 임시 코스가 성립하지 않는다).
            if (place.key == state.basePlaceKey) return@update state
            state.copy(orderedPlaces = state.orderedPlaces.filterNot { it.key == place.key })
        }
    }

    /**
     * 코스 순서 변경(드래그). 기준 장소도 다른 장소와 똑같이 옮길 수 있다.
     * 인덱스는 [CourseComposeUiState.orderedPlaces] 기준이며, 서버 반영은 순서 설정 화면 ✓ 에서 한 번에 한다.
     */
    fun onMovePlace(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val list = state.orderedPlaces.toMutableList()
            if (fromIndex !in list.indices || toIndex !in list.indices) return@update state
            list.add(toIndex, list.removeAt(fromIndex))
            state.copy(orderedPlaces = list)
        }
    }

    /**
     * 장소 선택 화면 헤더 ✓ → 순서 설정 화면 진입
     * (PATCH /api/course-drafts/{courseDraftId}/ordering).
     *
     * 멱등 API라 화면 재진입/재시도로 다시 불려도 안전하다. 응답의 courseDraftPlaceId 를
     * 담아둔 장소들에 맞춰 붙여야 이어지는 순서 변경 API 를 호출할 수 있다.
     */
    fun onSelectionConfirmed() {
        val state = _uiState.value
        if (!state.canConfirm || state.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            when (val result = courseDraftRepository.enterOrdering(courseDraftId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            orderedPlaces = it.orderedPlaces.withDraftPlaceIds(result.data.places),
                        )
                    }
                    _effect.send(CourseComposeEffect.NavigateToSelected)
                }

                is ApiResult.Failure ->
                    _uiState.update {
                        it.copy(isSubmitting = false, submitError = result.toUiError().message)
                    }
            }
        }
    }

    /**
     * 순서 설정 화면 헤더 ✓ → 최종 순서를 저장하고 코스 저장 화면으로 진입한다.
     *
     * 1. PATCH .../places/order — 화면에 보이는 최종 순서를 한 번에 전달
     * 2. PATCH .../saving — ORDERING → SAVING 전이 + 경로 미리보기 수신
     *
     * 1번은 서버가 부여한 courseDraftPlaceId 를 모두 알고 있을 때만 보낸다. id 는 선택 장소 추가 응답에서
     * 받아 두지만, 이전 세션에서 담긴 장소처럼 id 를 못 받은 항목이 섞여 있으면 순서 저장을 건너뛴다.
     *
     * ⚠️ 페이로드는 **기준 장소를 포함한 전체 순서를 visitOrder 1부터** 보낸다.
     * 명세는 "기준 장소를 빼고 visitOrder 2부터"라고 돼 있지만 배포 서버가 그 형태를 거부한다
     * (COURSE_ORDER400 "방문 순서가 올바르지 않습니다" — 1번이 비어 순서가 끊긴 것으로 본다).
     * 기준 장소를 옮기지 않은 기본 경로가 늘 여기 걸려 코스 저장까지 갈 수 없었다.
     * 전체를 1부터 보내는 형태는 기준 장소가 1번일 때도, 다른 자리로 옮겼을 때도 200 이다.
     */
    fun onOrderConfirmed() {
        val state = _uiState.value
        if (state.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            // 기준 장소를 포함한 전체 순서를 1번부터 보낸다. 위치와 무관하게 이 형태 하나만 쓴다.
            val draftPlaceIds = state.orderedPlaces.map { it.courseDraftPlaceId }
            if (draftPlaceIds.isNotEmpty() && draftPlaceIds.all { it != null }) {
                val orderResult = courseDraftRepository.updatePlaceOrder(
                    courseDraftId = courseDraftId,
                    orderedPlaceIds = draftPlaceIds.filterNotNull(),
                )
                if (orderResult is ApiResult.Failure) {
                    _uiState.update {
                        it.copy(isSubmitting = false, submitError = orderResult.toUiError().message)
                    }
                    return@launch
                }
            }

            when (val result = courseDraftRepository.enterSaving(courseDraftId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            orderedPlaces = it.orderedPlaces.withDraftPlaceIds(result.data.routePreview),
                        )
                    }
                    _effect.send(CourseComposeEffect.NavigateToSave)
                }

                is ApiResult.Failure ->
                    _uiState.update {
                        it.copy(isSubmitting = false, submitError = result.toUiError().message)
                    }
            }
        }
    }

    /**
     * 이전 버튼(`<`) → 단계 이동 API 호출 후 이전 화면으로 돌아간다.
     *
     * 코스 구성 플로우의 세 화면(장소 선택 PLACE_SELECTING · 순서 설정 ORDERING · 저장 SAVING)이
     * 이 ViewModel 을 공유하므로 어느 화면에서 눌렀는지를 [from] 으로 받아 그 이전 단계로 보낸다.
     *
     * 단순 화면 이동이라 담은 장소·순서를 지우지도, 알림을 띄우지도 않는다. 단계 이동 호출이
     * 실패해도 화면은 넘긴다 — 사용자에게는 뒤로 가기일 뿐이다.
     */
    fun onBackClick(from: CourseDraftStatus) {
        val target = from.previous
        viewModelScope.launch {
            if (target != null) courseDraftRepository.moveToStatus(courseDraftId, target)
            _effect.send(CourseComposeEffect.NavigateBack)
        }
    }

    fun onDismissAlert() {
        _uiState.update { it.copy(alert = null) }
    }

    /** 단계 전환 실패 안내를 닫는다. */
    fun onDismissSubmitError() {
        _uiState.update { it.copy(submitError = null) }
    }

    /**
     * 서버가 내려준 임시 코스 장소 목록에서 courseDraftPlaceId 를 찾아 담아둔 장소에 붙인다.
     *
     * id 는 선택 장소 추가 응답에서 이미 받지만, 단계 전환 응답이 서버 기준 최신값이라 한 번 더 맞춘다.
     * 매칭되지 않는 장소는 기존 값을 유지해 화면(로컬 순서)이 깨지지 않게 한다.
     *
     * 기준 장소(placeRole=BASE)도 서버 응답에 courseDraftPlaceId 를 달고 내려오므로 함께 매칭한다.
     * 기준 장소를 다른 자리로 옮겼을 때 순서 변경 요청에 넣으려면 이 id 가 있어야 한다.
     */
    private fun List<PlaceUiModel>.withDraftPlaceIds(draftPlaces: List<CourseDraftPlaceDto>): List<PlaceUiModel> {
        if (draftPlaces.isEmpty()) return this
        val idByPlaceId = draftPlaces.associate { it.placeId to it.courseDraftPlaceId }
        return map { place ->
            val draftPlaceId = place.placeId?.let(idByPlaceId::get) ?: return@map place
            place.copy(courseDraftPlaceId = draftPlaceId)
        }
    }

    companion object {
        private const val EMPTY_MESSAGE = "추천할 수 있는 장소가 없어요."

        // 선택 장소 추가 API 는 내부 placeId 만 받는다(카카오 미등록 장소는 담을 수 없다).
        private const val UNSUPPORTED_PLACE_MESSAGE = "지금은 담을 수 없는 장소예요."
    }
}

/** 코스 구성 플로우에서 화면 밖으로 나가는 일회성 효과(네비게이션). */
sealed interface CourseComposeEffect {
    /** 순서 설정(선택한 장소) 화면으로 이동. */
    data object NavigateToSelected : CourseComposeEffect

    /** 코스 저장 화면으로 이동. */
    data object NavigateToSave : CourseComposeEffect

    /** 이전 버튼(`<`) → 단계 이동 API 를 부른 뒤 이전 화면으로 돌아간다. */
    data object NavigateBack : CourseComposeEffect
}
