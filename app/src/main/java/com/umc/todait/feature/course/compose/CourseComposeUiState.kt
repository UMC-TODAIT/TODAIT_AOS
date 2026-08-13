package com.umc.todait.feature.course.compose

import com.umc.todait.core.location.Coordinate
import com.umc.todait.feature.course.base_place.PlaceUiModel
import com.umc.todait.feature.course.data.dto.CurrentDraftPlaceDto
import com.umc.todait.feature.course.data.dto.PlaceCategoryResponseDto
import com.umc.todait.feature.course.data.dto.PlaceDetailDto

/**
 * 코스 구성하기 플로우(#26, 와이어프레임 "코스구성하기_수정버전")의 공유 UI 상태.
 *
 * **두 화면이 이 상태를 공유한다**(NavHost 의 course/compose 중첩 그래프에 스코프된 ViewModel):
 * 1) [CourseComposeScreen] (장소카드 선택): [상단 지도] + [카테고리 탭] + [추천 카드(탭해서 담기)]. 헤더 ✓ → 2)로 이동.
 * 2) [com.umc.todait.feature.course.compose.SelectedPlacesScreen] (선택한 장소): [지도] + "선택한 장소(N)" 드래그 순서 조정. 헤더 ✓ → 코스 저장.
 *
 * - 기준 장소([basePlace], #11에서 확정)를 출발점으로, 카테고리별 추천 장소를 [recommendState] 로 노출.
 * - 카드 탭으로 코스에 담고([selectedPlaces], 담은 순서 = 코스 순서), 2) 화면에서 드래그로 순서를 수정한다.
 *   (담긴 카드는 Green-700 테두리 + Click 그라데이션으로 표시하고 탭을 무시한다.)
 * - 이미 담긴 장소를 다시 담으면 [alert] 로 중복 안내.
 *
 * ⚠️ 지도는 카카오맵 v2([CourseMap])로 연동돼 있다(런타임 키/렌더 확인은 별도).
 * 드래그 순서 변경 제스처/카테고리별 추천 필터/임시 코스 세션 연동은 TODO 로 남겨 후속 확장한다.
 */
data class CourseComposeUiState(
    // 임시 코스(course-draft) 핸들. 기준 장소 설정 화면에서 확정돼 경로 변수로 넘어온다.
    val courseDraftId: Long,
    // 코스 동선 순서대로의 **전체** 장소 목록. 기준 장소도 이 안에 들어 있고 드래그 대상이다.
    // 지도 핀 번호·순번 배지는 모두 이 목록의 index + 1 이다.
    val orderedPlaces: List<PlaceUiModel> = emptyList(),
    // [orderedPlaces] 안에서 기준 장소가 누구인지 가리키는 key. 상세 조회 전이거나 실패하면 null.
    // 순서를 바꾸면 기준 장소가 첫 번째가 아닐 수도 있어 "index 0" 대신 key 로 들고 있는다.
    val basePlaceKey: String? = null,
    // 지도에 현재 위치 마커를 찍기 위한 좌표. 위치 권한 없음/조회 실패면 null(마커 미표시).
    val currentLocation: Coordinate? = null,
    // 카테고리 탭(장소 대분류). GET /api/place-categories 로 로드. 비어 있으면 탭 미표시.
    val categories: List<PlaceCategoryUiModel> = emptyList(),
    // 현재 선택된 카테고리 id. null 이면 미선택.
    val selectedCategoryId: Long? = null,
    val recommendState: RecommendListState = RecommendListState.Loading,
    // 선택 장소 추가(POST .../places) 요청이 진행 중인 장소의 [PlaceUiModel.key]. 같은 카드의 중복 탭을 막는다.
    val addingPlaceKeys: Set<String> = emptySet(),
    // 노출 중인 시스템 알럿(중복 선택 등). null 이면 닫힘.
    val alert: CourseComposeAlert? = null,
    // 다음 단계 전환 API(ordering / places-order / saving) 호출 중. 헤더 ✓ 중복 탭을 막는다.
    val isSubmitting: Boolean = false,
    // 단계 전환 실패 안내 문구. null 이면 없음.
    val submitError: String? = null,
) {
    /** 기준 장소. 순서를 바꿨다면 [orderedPlaces] 의 첫 번째가 아닐 수도 있다. */
    val basePlace: PlaceUiModel? get() = orderedPlaces.firstOrNull { it.key == basePlaceKey }

    /**
     * 사용자가 추천 카드로 담은 장소들(기준 장소 제외). 담기/중복 검사와
     * 순서 변경 API 페이로드 구성에 쓴다. 순서는 [orderedPlaces] 를 따른다.
     */
    val selectedPlaces: List<PlaceUiModel> get() = orderedPlaces.filterNot { it.key == basePlaceKey }

    /** 기준 장소가 코스의 첫 장소인지. 순서 변경 API 페이로드 모양이 이 값에 따라 달라진다. */
    val isBasePlaceFirst: Boolean
        get() = basePlaceKey == null || orderedPlaces.firstOrNull()?.key == basePlaceKey

    /** 담은 장소가 하나라도 있어야 확정(다음 단계) 가능. */
    val canConfirm: Boolean get() = selectedPlaces.isNotEmpty()

    /** 현재 선택된 카테고리 탭. 추천 조회에 필요한 code 를 여기서 얻는다. */
    val selectedCategory: PlaceCategoryUiModel?
        get() = categories.firstOrNull { it.id == selectedCategoryId }
}

/**
 * 카테고리 탭(장소 대분류: 카페/식당/액티비티/술). GET /api/place-categories 매핑.
 * 하드코딩 enum 을 대체하며, 라벨/개수는 서버 응답을 따른다.
 */
data class PlaceCategoryUiModel(
    val id: Long,
    // 대분류 코드(CAFE/RESTAURANT/ACTIVITY/BAR). 추천 조회 API 의 placeCategoryCode 로 전달한다.
    val code: String,
    val name: String,
)

/** 장소 대분류 DTO → 화면 탭 모델. */
fun PlaceCategoryResponseDto.toUiModel(): PlaceCategoryUiModel = PlaceCategoryUiModel(
    id = placeCategoryId,
    code = code,
    name = name,
)

/**
 * 장소 상세 DTO → 기준 장소 화면 모델.
 * 기준 장소는 지도 1번 핀과 경로 미리보기 첫 줄에만 쓰여 추천 이유·분위기 태그가 필요 없다.
 */
fun PlaceDetailDto.toBasePlaceUiModel(): PlaceUiModel = PlaceUiModel(
    placeId = placeId,
    name = name,
    address = roadAddress?.takeIf { it.isNotBlank() } ?: address,
    category = placeCategory.name,
    // 상세 응답에는 지원 지역 정보가 없다. 지역 검증은 기준 장소 설정 시 이미 끝났으므로 비워 둔다.
    areaName = "",
    imageUrl = imageUrls.firstOrNull() ?: defaultImageUrl,
    reasonText = null,
    latitude = latitude,
    longitude = longitude,
    jibunAddress = address,
    roadAddress = roadAddress,
    categoryCode = placeCategory.code,
    subCategory = subCategory,
    phone = phone,
)

/**
 * 진행 중 임시 코스의 장소 DTO → 코스 구성 화면 모델.
 *
 * "이어서 하기"로 코스 구성 단계에 복귀할 때 담아뒀던 장소를 되살리는 데 쓴다.
 * 응답에 카드 렌더에 필요한 이름·주소·좌표·이미지·지역·카테고리가 모두 들어 있어
 * 장소 상세를 다시 조회하지 않아도 된다.
 *
 * [PlaceUiModel.courseDraftPlaceId] 를 함께 채워야 순서 변경 요청을 바로 보낼 수 있다.
 */
fun CurrentDraftPlaceDto.toUiModel(): PlaceUiModel = PlaceUiModel(
    placeId = placeId,
    name = name,
    address = roadAddress?.takeIf { it.isNotBlank() } ?: address,
    category = category?.name.orEmpty(),
    areaName = area?.name.orEmpty(),
    imageUrl = imageUrl?.takeIf { it.isNotBlank() },
    reasonText = null,
    latitude = latitude,
    longitude = longitude,
    jibunAddress = address,
    roadAddress = roadAddress,
    areaCode = area?.code.orEmpty(),
    categoryCode = category?.code.orEmpty(),
    subCategory = subCategory,
    courseDraftPlaceId = courseDraftPlaceId,
)

/**
 * 추천 장소 카드의 분위기(mood). Figma 카드 컴포넌트가 분위기별로 다른 그라데이션/장식을 쓴다.
 *
 * 종류는 "분위기 태그 조회"(GET /api/mood-tags) 명세의 6종(mood_tag)과 일치시킨다.
 * [code] = mood_tag.code, [label] = mood_tag.name.
 * 그라데이션 색은 Figma "취향설정" 화면 기준으로 6종 전부 확정(색상 토큰은 Color.kt 참고).
 * 우측 하단 아이콘도 6종 각각 전용 에셋(ic_mood_*)으로 매칭한다.
 *
 * [PlaceUiModel.moodTags] 의 분위기 태그(code 또는 name)로 결정한다([fromTags]). 카테고리별 추천 응답의
 * matchedMoodTags 가 비어 있을 수도 있어(일치 태그 없음) 그때는 화면에서 fallback 을 부여한다.
 */
enum class CourseMood(val code: String, val label: String) {
    HIP("HIP", "힙한"),
    QUIET("QUIET", "조용한"),
    ACTIVE("ACTIVE", "활발한"),
    ROMANTIC("ROMANTIC", "로맨틱"),
    MODERN("MODERN", "모던한"),
    CALM("CALM", "차분한"),
    ;

    companion object {
        /** 분위기 태그 목록(code 또는 name)에서 첫 번째로 매칭되는 분위기를 찾는다. 없으면 null. */
        fun fromTags(tags: List<String>): CourseMood? =
            tags.firstNotNullOfOrNull { raw ->
                val t = raw.trim()
                entries.firstOrNull { it.code.equals(t, ignoreCase = true) || it.label == t }
            }
    }
}

/** 추천 장소 목록 영역의 상태. */
sealed interface RecommendListState {
    data object Loading : RecommendListState
    data class Success(val places: List<PlaceUiModel>) : RecommendListState
    data class Empty(val message: String) : RecommendListState
    data class Error(val message: String) : RecommendListState
}

/** 코스 구성하기 화면의 시스템 알럿 종류. */
sealed interface CourseComposeAlert {
    /** 이미 담은 장소를 다시 담으려 할 때(중복선택 알럿). 요청 전에 화면에서 걸러낸 경우다. */
    data object Duplicate : CourseComposeAlert

    /**
     * 선택 장소 추가(POST .../places)가 실패했을 때.
     * [message] 는 서버가 준 안내 문구를 그대로 쓴다(중복·기준 장소 재선택·최대 개수 초과 등).
     */
    data class AddFailed(val message: String) : CourseComposeAlert
}
