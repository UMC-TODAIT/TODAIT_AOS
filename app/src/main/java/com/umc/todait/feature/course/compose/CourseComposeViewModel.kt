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
 * - [onTogglePlace] (추천 카드 탭): 화면에서만 담고/뺀다. 서버 호출 없음
 * - [onSelectionConfirmed] (장소 선택 ✓): 담은 장소를 POST .../places 로 일괄 커밋한 뒤
 *   PATCH .../ordering — PLACE_SELECTING → ORDERING
 * - [onOrderConfirmed] (순서 설정 ✓): PATCH .../places/order 로 최종 순서 저장 후 PATCH .../saving
 *
 * ## ⚠️ 커밋 지연(deferred commit) — 데모 시연용 우회
 *
 * 원래는 추천 카드를 탭하는 즉시 POST .../places 로 서버에 담았다. 그런데 **선택 장소 삭제
 * (DELETE .../places/{courseDraftPlaceId})가 아직 배포되지 않아** 한 번 담으면 뺄 방법이 없다.
 * 화면에서만 빼면 서버에는 남아 (1) 최종 코스에 그대로 저장되고(코스 저장 요청은 임시 코스에 담긴
 * 장소를 그대로 쓴다), (2) 순서 변경 요청의 visitOrder 가 끊겨 400 이 난다.
 *
 * 그래서 이 브랜치는 **탭 시점에 서버를 건드리지 않고**, ✓ 를 누를 때 선택한 장소를 화면 순서대로
 * 한 번에 POST 한다. ✓ 이전의 선택/취소는 완전히 로컬이라 자유롭게 되돌릴 수 있다.
 *
 * 대신 두 가지를 감수한다.
 * - ✓ 전에 앱을 벗어나면 선택 내역이 사라진다(서버에 아직 없다).
 * - "이어서 하기"로 복귀해 서버에서 되살린 장소([restoreDraftPlaces])는 이미 커밋된 상태라
 *   여전히 뺄 수 없다 — 삭제 API 가 나오기 전까지는 방법이 없다.
 *
 * 삭제 API 가 배포되면 이 우회를 걷어내고 탭 즉시 담기/빼기로 되돌리면 된다.
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
     * 추천 카드 탭 → 코스에 담기 / 담은 장소 취소. **서버 호출은 하지 않는다.**
     *
     * 담기는 목록 끝에 붙이고(담은 순서 = 코스 순서), 이미 담은 장소를 다시 탭하면 뺀다.
     * 서버 반영은 ✓([onSelectionConfirmed])에서 한 번에 하므로 여기서는 화면 상태만 바꾼다.
     * (이유는 클래스 KDoc 의 "커밋 지연" 참고.)
     *
     * 서버가 POST .../places 에서 막는 두 가지(같은 장소 중복·기준 장소 재추가)는 여기서 이미
     * 성립하지 않는다 — 담은 장소를 다시 탭하면 취소가 되고, 기준 장소는 추천 목록에 없다.
     * 카테고리당 1곳 같은 제약은 배포 서버에 없다(2026-08-16 실측).
     */
    fun onTogglePlace(place: PlaceUiModel) {
        val state = _uiState.value
        // 커밋(POST) 중인 장소는 건드리지 않는다.
        if (place.key in state.addingPlaceKeys) return

        val selected = state.selectedPlaces.firstOrNull { it.key == place.key }
        if (selected != null) {
            onRemovePlace(selected)
            return
        }

        // 추천 카드는 운영자 검수를 마친 내부 장소라 placeId 가 항상 있다(외부 장소는 담을 수 없다).
        if (place.placeId == null) {
            _uiState.update { it.copy(alert = CourseComposeAlert.AddFailed(UNSUPPORTED_PLACE_MESSAGE)) }
            return
        }
        _uiState.update { it.copy(orderedPlaces = it.orderedPlaces + place) }
    }

    /**
     * 담은 장소 취소. 아직 서버에 올리지 않은 장소만 뺄 수 있다.
     *
     * ⚠️ 이미 커밋된 장소(courseDraftPlaceId 가 있는 장소 — ✓ 를 눌러 POST 했거나 "이어서 하기"로
     * 되살린 장소)는 삭제 API(DELETE .../places/{courseDraftPlaceId})가 없어서 뺄 수 없다.
     * 화면에서만 빼면 최종 코스에 그대로 남으므로, 지우는 대신 안내 알럿을 띄운다.
     */
    fun onRemovePlace(place: PlaceUiModel) {
        _uiState.update { state ->
            // 기준 장소는 화면에서 뺄 수 없다(코스의 기준점이라 빼면 임시 코스가 성립하지 않는다).
            if (place.key == state.basePlaceKey) return@update state
            if (place.courseDraftPlaceId != null) {
                return@update state.copy(alert = CourseComposeAlert.RemoveUnavailable)
            }
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
     * 장소 선택 화면 헤더 ✓ → 담은 장소를 서버에 커밋한 뒤 순서 설정 화면으로 진입한다.
     *
     * 1. POST .../places — 아직 서버에 없는 장소를 **화면에 보이는 순서대로** 하나씩 담는다.
     *    서버가 visitOrder 를 (현재 최댓값 + 1)로 매기므로 이 순서가 그대로 코스 동선이 된다.
     * 2. PATCH .../ordering — PLACE_SELECTING → ORDERING. 멱등이라 재시도해도 안전하고,
     *    응답의 courseDraftPlaceId 를 담아둔 장소들에 맞춰 붙여야 순서 변경 API 를 호출할 수 있다.
     *
     * 1번이 도중에 실패하면 거기서 멈추고 서버 문구를 그대로 띄운다. 이미 커밋된 장소는 되돌릴
     * 방법이 없어(삭제 API 미배포) 그대로 두고, 사용자가 ✓ 를 다시 누르면 남은 장소만 이어서 담는다.
     */
    fun onSelectionConfirmed() {
        val state = _uiState.value
        if (!state.canConfirm || state.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            if (!commitPendingPlaces()) return@launch

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
     * 화면에서 담아둔 장소 중 아직 서버에 없는 것을 화면 순서대로 POST .../places 로 커밋한다.
     *
     * 순차 호출인 이유는 서버가 visitOrder 를 (현재 최댓값 + 1)로 매기기 때문이다 — 동시에 보내면
     * 담은 순서가 코스 동선과 어긋난다. 응답으로 받은 courseDraftPlaceId 를 바로 붙여 두면
     * 재시도 시 같은 장소를 두 번 담지 않는다.
     *
     * @return 전부 성공하면 true. 하나라도 실패하면 [CourseComposeUiState.submitError] 를 채우고 false.
     */
    private suspend fun commitPendingPlaces(): Boolean {
        val pending = _uiState.value.selectedPlaces.filter { it.courseDraftPlaceId == null }
        for (place in pending) {
            // onTogglePlace 에서 이미 걸러지지만, 서버에 보낼 수 없는 장소가 남아 있으면 건너뛴다.
            val placeId = place.placeId ?: continue

            _uiState.update { it.copy(addingPlaceKeys = it.addingPlaceKeys + place.key) }
            val result = courseDraftRepository.addPlace(courseDraftId = courseDraftId, placeId = placeId)
            _uiState.update { it.copy(addingPlaceKeys = it.addingPlaceKeys - place.key) }

            when (result) {
                is ApiResult.Success -> _uiState.update { state ->
                    val draftPlaceId = result.data.addedPlace.courseDraftPlaceId
                    state.copy(
                        orderedPlaces = state.orderedPlaces.map {
                            if (it.key == place.key) it.copy(courseDraftPlaceId = draftPlaceId) else it
                        },
                    )
                }

                // 서버 message 가 그대로 안내 문구가 된다(예: "이미 선택한 장소입니다.").
                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, submitError = result.toUiError().message)
                    }
                    return false
                }
            }
        }
        return true
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
