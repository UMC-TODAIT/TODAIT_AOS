package com.umc.todait.feature.course.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.base_place.PlaceUiModel
import com.umc.todait.feature.course.base_place.toUiModel
import com.umc.todait.feature.course.data.dto.CourseDraftPlaceDto
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
 * - [onSelectionConfirmed] (장소 선택 ✓): PATCH .../ordering — PLACE_SELECTING → ORDERING
 * - [onOrderConfirmed] (순서 설정 ✓): PATCH .../places/order 로 최종 순서 저장 후 PATCH .../saving
 */
@HiltViewModel
class CourseComposeViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val placeCategoryRepository: PlaceCategoryRepository,
    private val courseDraftRepository: CourseDraftRepository,
    private val placeRepository: PlaceRepository,
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
        loadBasePlace()
        // 카테고리(탭)가 있어야 추천을 조회할 수 있어(placeCategoryCode 필요) 카테고리 로드 후 추천을 부른다.
        loadCategories()
    }

    /**
     * 기준 장소 상세 조회(GET /api/places/{placeId}).
     *
     * 기준 장소 설정 API 응답에는 지도/경로 미리보기에 필요한 정보가 요약만 들어 있어,
     * 확정된 placeId 로 장소 카드 상세를 한 번 더 불러 화면 모델을 채운다.
     * 실패해도 추천 목록은 볼 수 있어야 하므로 목록 상태는 건드리지 않는다.
     */
    private fun loadBasePlace() {
        viewModelScope.launch {
            when (val result = placeRepository.getPlaceDetail(basePlaceId)) {
                is ApiResult.Success -> _uiState.update { it.copy(basePlace = result.data.toBasePlaceUiModel()) }
                is ApiResult.Failure -> Unit
            }
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
     * 추천 카드 '+' → 코스에 담기.
     * 이미 담긴 장소면 중복 알럿을 띄우고, 아니면 목록 끝에 추가한다.
     *
     * TODO(BE 멍이): "선택 장소 추가"(POST /api/course-drafts/{id}/places)가 개발 완료되면
     *  여기서 호출해 courseDraftPlaceId 를 서버에서 받아 [PlaceUiModel.courseDraftPlaceId] 에 채운다.
     *  그 전까지는 로컬 목록만 유지하고, 순서 설정 진입 응답에서 id 를 맞춰 붙인다.
     */
    fun onAddPlace(place: PlaceUiModel) {
        _uiState.update { state ->
            if (state.selectedPlaces.any { it.key == place.key }) {
                state.copy(alert = CourseComposeAlert.Duplicate)
            } else {
                state.copy(selectedPlaces = state.selectedPlaces + place)
            }
        }
    }

    /** 선택한 장소에서 빼기. */
    fun onRemovePlace(place: PlaceUiModel) {
        _uiState.update { state ->
            state.copy(selectedPlaces = state.selectedPlaces.filterNot { it.key == place.key })
        }
    }

    /** 선택한 장소 순서 변경(드래그). 서버 반영은 순서 설정 화면 ✓ 에서 한 번에 한다. */
    fun onMovePlace(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val list = state.selectedPlaces.toMutableList()
            if (fromIndex !in list.indices || toIndex !in list.indices) return@update state
            list.add(toIndex, list.removeAt(fromIndex))
            state.copy(selectedPlaces = list)
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
                            selectedPlaces = it.selectedPlaces.withDraftPlaceIds(result.data.places),
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
     * 1. PATCH .../places/order — 기준 장소를 제외한 선택 장소 전체의 최종 순서를 한 번에 전달
     * 2. PATCH .../saving — ORDERING → SAVING 전이 + 경로 미리보기 수신
     *
     * 1번은 서버가 부여한 courseDraftPlaceId 를 모두 알고 있을 때만 보낸다. 아직 "선택 장소 추가" API 가
     * 없어 id 를 못 받은 장소가 섞여 있으면 순서 저장을 건너뛰고 저장 화면 진입만 진행한다.
     */
    fun onOrderConfirmed() {
        val state = _uiState.value
        if (state.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val draftPlaceIds = state.selectedPlaces.map { it.courseDraftPlaceId }
            if (draftPlaceIds.isNotEmpty() && draftPlaceIds.all { it != null }) {
                val orderResult = courseDraftRepository.updatePlaceOrder(
                    courseDraftId = courseDraftId,
                    selectedPlaceIds = draftPlaceIds.filterNotNull(),
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
                            selectedPlaces = it.selectedPlaces.withDraftPlaceIds(result.data.routePreview),
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
     * "선택 장소 추가" API 가 아직 없어 서버가 앱의 선택 목록을 모르는 동안에는 매칭되지 않는 장소가
     * 생길 수 있다. 그때는 기존 값을 유지해 화면(로컬 순서)이 깨지지 않게 한다.
     */
    private fun List<PlaceUiModel>.withDraftPlaceIds(draftPlaces: List<CourseDraftPlaceDto>): List<PlaceUiModel> {
        if (draftPlaces.isEmpty()) return this
        // 기준 장소(BASE)는 선택 장소 목록에 없으므로 매칭 대상에서 제외한다.
        val idByPlaceId = draftPlaces.filterNot { it.isBase }.associate { it.placeId to it.courseDraftPlaceId }
        return map { place ->
            val draftPlaceId = place.placeId?.let(idByPlaceId::get) ?: return@map place
            place.copy(courseDraftPlaceId = draftPlaceId)
        }
    }

    companion object {
        private const val EMPTY_MESSAGE = "추천할 수 있는 장소가 없어요."
    }
}

/** 코스 구성 플로우에서 화면 밖으로 나가는 일회성 효과(네비게이션). */
sealed interface CourseComposeEffect {
    /** 순서 설정(선택한 장소) 화면으로 이동. */
    data object NavigateToSelected : CourseComposeEffect

    /** 코스 저장 화면으로 이동. */
    data object NavigateToSave : CourseComposeEffect
}
