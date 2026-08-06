package com.umc.todait.feature.course.base_place

import com.umc.todait.feature.course.data.dto.ExternalPlaceDto
import com.umc.todait.feature.course.data.dto.HotPlaceDto
import com.umc.todait.feature.course.data.dto.RecommendedPlaceDto
import com.umc.todait.feature.course.data.dto.SearchPlaceDto

/**
 * 기준 장소 설정 화면의 UI 상태.
 *
 * 화면 명세(와이어프레임 1.1)에 따라 검색어가 비어 있으면 "지금 내 주변 핫플" 추천 목록을,
 * 검색어가 있으면 검색 결과 목록을 [listState] 로 표현한다.
 *
 * 상호작용(Figma): 카드 우상단 '+' → [selectedPlace] 선택(초록 테두리), 헤더 체크 → [alert] 노출.
 * 미선택 상태 확정 시 [BasePlaceAlert.SelectRequired](시스템알럿1),
 * 선택 상태 확정 시 [BasePlaceAlert.Confirm](시스템알럿2)이 뜬다.
 */
data class BasePlaceUiState(
    val searchQuery: String = "",
    // 현재 지역 표시(예: 마포구). 위치 권한 플로우는 이번 범위에서 제외라 기본 null(미표시).
    val currentAreaName: String? = null,
    val listState: PlaceListState = PlaceListState.Loading,
    // 현재 선택된 기준 장소(카드 '+' 로 선택). null 이면 미선택.
    val selectedPlace: PlaceUiModel? = null,
    // 노출 중인 시스템 알럿. null 이면 닫힘.
    val alert: BasePlaceAlert? = null,
    // 확정 알럿 안에서 노출할 예외 안내 문구(지원 지역 외/좌표 없음 등).
    val confirmError: String? = null,
    // 기준 장소 설정 API 호출 중. 확정 알럿의 [확인] 중복 탭을 막는다.
    val isConfirming: Boolean = false,
) {
    /** 검색어가 없으면 추천 섹션, 있으면 검색 결과 섹션. */
    val isSearching: Boolean get() = searchQuery.isNotBlank()
}

/** 기준 장소 설정 화면의 시스템 알럿 종류. */
sealed interface BasePlaceAlert {
    /** 시스템알럿1: 기준 장소 미선택 상태에서 확정(체크)을 눌렀을 때. */
    data object SelectRequired : BasePlaceAlert

    /** 시스템알럿2: 선택된 [place] 로 확정할지 확인. */
    data class Confirm(val place: PlaceUiModel) : BasePlaceAlert
}

/** 장소 목록 영역의 상태. 추천/검색 결과가 공통으로 사용한다. */
sealed interface PlaceListState {
    data object Loading : PlaceListState
    data class Success(val places: List<PlaceUiModel>) : PlaceListState
    /** 결과 없음(검색 결과 없음/추천 없음). [message] 는 화면 안내 문구. */
    data class Empty(val message: String) : PlaceListState
    data class Error(val message: String) : PlaceListState
}

/**
 * 화면에 노출하는 장소 카드 모델.
 * DTO(SearchPlaceDto / HotPlaceDto / RecommendedPlaceDto)를 화면 표현용으로 매핑한다.
 * (컨벤션 §5: DTO는 data 안에서만)
 *
 * 명세 정책상 별점/평점/내부 점수는 담지 않으며, 신뢰도는 [reasonText](추천 이유)로만 표현한다.
 *
 * ⚠️ 검색 결과에는 내부 DB에 없는 카카오 장소도 섞여 내려온다. 그런 장소는 [placeId] 가 null 이고
 * [externalPlaceId] 만 있으므로, 목록 key·선택 비교에는 [placeId] 대신 [key] 를 사용한다.
 */
data class PlaceUiModel(
    val placeId: Long?,
    val name: String,
    val address: String,
    val category: String,
    val areaName: String,
    val imageUrl: String?,
    val reasonText: String?,
    val latitude: Double,
    val longitude: Double,
    // 분위기 태그(예: "로맨틱", "모던한"). 코스 구성 추천에서만 내려오며, 없으면 빈 리스트.
    // 코스 구성 카드의 분위기별 색상 결정에 쓰인다.
    val moodTags: List<String> = emptyList(),
    // 내부 DB 미등록 장소의 식별자(카카오 장소 ID). 기준 장소 설정 시 externalPlace 로 전달한다.
    val externalPlaceId: String? = null,
    // 장소 상세 화면으로 진입할 수 있는지. false 면 카드 탭을 무시한다.
    val detailAvailable: Boolean = true,
    // --- 아래는 화면에 그리지 않고 "기준 장소 설정"(PATCH .../base-place) 요청에만 쓰는 값들이다. ---
    // 지번 주소. 화면 표시용 [address] 는 도로명 우선이라 원본을 따로 들고 있어야 한다(요청의 address 는 지번).
    val jibunAddress: String = "",
    val roadAddress: String? = null,
    // 지원 지역 코드(HONGDAE/YEONNAM/SEONGSU). 서버 응답 값을 그대로 되돌려 보낸다.
    val areaCode: String = "",
    // 장소 대분류 코드(CAFE/RESTAURANT/ACTIVITY/BAR). 서버 응답 값을 그대로 되돌려 보낸다.
    val categoryCode: String = "",
    val subCategory: String? = null,
    val phone: String? = null,
    // 카카오 장소 상세 페이지 URL.
    val sourceUrl: String? = null,
    // 임시 코스에 담긴 뒤 서버가 부여한 연결 행 ID(course_draft_place.id).
    // 순서 변경 API 는 placeId 가 아니라 이 값을 쓴다. 담기 전이면 null.
    val courseDraftPlaceId: Long? = null,
) {
    /** 목록 key·선택 비교용 식별자. 내부 장소는 placeId, 미등록 장소는 외부 ID를 사용한다. */
    val key: String get() = placeId?.toString() ?: "external:$externalPlaceId"
}

/**
 * 검색 결과 DTO → 화면 모델. 검색 결과에는 추천 이유가 없어 [reasonText] 는 null.
 *
 * - category 는 서버가 내부 대분류로 변환해준 [SearchPlaceDto.category] 의 name.
 * - areaName 은 지원 지역([SearchPlaceDto.area]) 이름 — 기준 장소 확정 시 지원 지역 검증에 쓴다.
 * - 분위기 태그는 검색 응답에 없어 비운다.
 */
fun SearchPlaceDto.toUiModel(): PlaceUiModel = PlaceUiModel(
    placeId = placeId,
    name = name,
    address = roadAddress?.takeIf { it.isNotBlank() } ?: address,
    category = category.name,
    areaName = area.name,
    // 빈 문자열이면 이미지 없음으로 보고 카드 그라데이션으로 렌더한다.
    imageUrl = imageUrl.takeIf { it.isNotBlank() },
    reasonText = null,
    latitude = latitude,
    longitude = longitude,
    externalPlaceId = externalPlaceId,
    detailAvailable = detailAvailable,
    jibunAddress = address,
    roadAddress = roadAddress,
    areaCode = area.code,
    categoryCode = category.code,
    subCategory = subCategory,
    phone = phone,
    sourceUrl = sourceUrl,
)

/**
 * 화면 모델 → 기준 장소 설정 요청의 externalPlace.
 *
 * 내부 DB 미등록 장소([PlaceUiModel.placeId] 가 null)일 때만 쓴다. 등록된 장소는 placeId 로 보낸다.
 * areaCode·categoryCode 는 앱이 주소/카테고리를 변환한 값이 아니라 **검색 API 응답 값 그대로**다.
 *
 * 필수값(sourcePlaceId·areaCode·categoryCode)이 비어 있으면 서버가 400 을 주므로 null 을 돌려준다.
 */
fun PlaceUiModel.toExternalPlaceDto(): ExternalPlaceDto? {
    val sourcePlaceId = externalPlaceId?.takeIf { it.isNotBlank() } ?: return null
    if (areaCode.isBlank() || categoryCode.isBlank()) return null
    return ExternalPlaceDto(
        dataSourceCode = DATA_SOURCE_KAKAO,
        sourcePlaceId = sourcePlaceId,
        name = name,
        // 명세상 address 는 지번 주소다. 화면 표시용 address(도로명 우선)를 그대로 보내면 안 된다.
        address = jibunAddress.takeIf { it.isNotBlank() } ?: address,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude,
        areaCode = areaCode,
        categoryCode = categoryCode,
        subCategory = subCategory,
        phone = phone,
        sourceUrl = sourceUrl,
    )
}

/** 외부 장소 데이터 출처 코드. MVP 는 카카오만 허용한다. */
private const val DATA_SOURCE_KAKAO = "KAKAO"

/**
 * 주변 핫플 DTO → 화면 모델.
 * 추천 문구([HotPlaceDto.recommendationReason])는 서버가 준 문자열을 그대로 노출한다.
 * 분위기 태그는 이 응답에 없어([matchedMoodCount] 만 존재) 비운다.
 */
fun HotPlaceDto.toUiModel(): PlaceUiModel = PlaceUiModel(
    placeId = placeId,
    name = name,
    address = roadAddress?.takeIf { it.isNotBlank() } ?: address,
    category = category.name,
    areaName = area.name,
    imageUrl = imageUrl,
    reasonText = recommendationReason,
    latitude = latitude,
    longitude = longitude,
    detailAvailable = detailAvailable,
    jibunAddress = address,
    roadAddress = roadAddress,
    areaCode = area.code,
    categoryCode = category.code,
    subCategory = subCategory,
)

/**
 * 카테고리별 추천 장소 DTO → 화면 모델.
 * 추천 이유는 최대 2개가 우선순위 순으로 내려오며, 카드에는 첫 문구만 노출한다.
 */
fun RecommendedPlaceDto.toUiModel(): PlaceUiModel = PlaceUiModel(
    placeId = placeId,
    name = name,
    address = roadAddress?.takeIf { it.isNotBlank() } ?: address,
    category = category.name,
    areaName = area.name,
    imageUrl = imageUrl,
    reasonText = recommendationReasons.firstOrNull(),
    latitude = latitude,
    longitude = longitude,
    moodTags = matchedMoodTags.map { it.name },
    detailAvailable = detailAvailable,
    jibunAddress = address,
    roadAddress = roadAddress,
    areaCode = area.code,
    categoryCode = category.code,
    subCategory = subCategory,
)
