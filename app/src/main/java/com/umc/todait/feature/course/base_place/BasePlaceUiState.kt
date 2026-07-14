package com.umc.todait.feature.course.base_place

import com.umc.todait.feature.course.data.dto.PlaceDto
import com.umc.todait.feature.course.data.dto.RecommendedPlaceDto

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
 * DTO(PlaceDto / RecommendedPlaceDto)를 화면 표현용으로 매핑한다. (컨벤션 §5: DTO는 data 안에서만)
 *
 * 명세 정책상 별점/평점/내부 점수는 담지 않으며, 신뢰도는 [reasonText](추천 이유)로만 표현한다.
 */
data class PlaceUiModel(
    val placeId: Long,
    val name: String,
    val address: String,
    val category: String,
    val areaName: String,
    val imageUrl: String?,
    val reasonText: String?,
    val latitude: Double,
    val longitude: Double,
)

/** 검색 결과 DTO → 화면 모델. 검색 결과에는 추천 이유가 없어 [reasonText] 는 null. */
fun PlaceDto.toUiModel(): PlaceUiModel = PlaceUiModel(
    placeId = placeId,
    name = name,
    address = address,
    category = category,
    areaName = areaName,
    imageUrl = imageUrl,
    reasonText = null,
    latitude = latitude,
    longitude = longitude,
)

/** 추천 장소 DTO → 화면 모델. RecommendedPlaceDto 에는 areaName 이 없어 빈 문자열로 둔다. */
fun RecommendedPlaceDto.toUiModel(): PlaceUiModel = PlaceUiModel(
    placeId = placeId,
    name = name,
    address = address,
    category = category,
    areaName = "",
    imageUrl = imageUrl,
    reasonText = reasonText,
    latitude = latitude,
    longitude = longitude,
)
