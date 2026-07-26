package com.umc.todait.feature.course.place_detail

import com.umc.todait.feature.course.data.dto.PlaceDetailDto
import com.umc.todait.feature.course.data.dto.PlaceMenuDto
import java.text.DecimalFormat

/**
 * 장소 상세 화면(와이어프레임: 장소카드클릭_기본)의 UI 상태.
 *
 * 조회 결과를 [detailState] 로 표현한다. 로딩/에러/성공을 명시적으로 구분해
 * 화면에서 로딩 인디케이터·에러 재시도·본문을 분기한다.
 */
data class PlaceDetailUiState(
    val detailState: PlaceDetailState = PlaceDetailState.Loading,
)

/** 장소 상세 조회 상태. */
sealed interface PlaceDetailState {
    data object Loading : PlaceDetailState
    data class Success(val place: PlaceDetailUiModel) : PlaceDetailState
    data class Error(val message: String) : PlaceDetailState
}

/**
 * 장소 상세 화면에 노출하는 모델. DTO(PlaceDetailDto)를 화면 표현용으로 매핑한다.
 * (컨벤션 §5: DTO 는 data 안에서만 사용)
 *
 * 명세 정책상 별점/평점/내부 점수는 담지 않는다.
 */
data class PlaceDetailUiModel(
    val placeId: Long,
    val name: String,
    // 카테고리 표기(예: "카페 · 디저트"). subCategory 가 있으면 함께 노출한다.
    val categoryLabel: String,
    // 도로명 주소 우선, 없으면 지번 주소.
    val address: String,
    val phone: String?,
    // 캐러셀에 노출할 이미지 URL(MAIN 이미지). 비어 있으면 defaultImageUrl 로 대체한다.
    val imageUrls: List<String>,
    // "내부 사진" 섹션 전용 이미지. 비어 있으면 섹션을 숨긴다.
    val interiorImageUrls: List<String> = emptyList(),
    // 추천 이유(예: "현재 위치와 가까워요"). 없으면 null.
    val recommendReason: String?,
    // 분위기/음식 해시태그(예: "#낭만적인", "#디저트").
    val hashTags: List<String>,
    // 영업 상태 라벨(예: "영업중"). 없으면 영업 정보 행 자체를 숨긴다.
    val openStatus: String? = null,
    // 라스트 오더 안내(예: "20:30에 라스트 오더"). openStatus 와 함께 노출.
    val lastOrderText: String? = null,
    // 메뉴 카드(썸네일·이름·가격). 비어 있으면 메뉴 섹션을 숨긴다.
    val menus: List<MenuUiItem> = emptyList(),
) {
    /** 내부 사진 전체보기로 넘길 이미지가 있는지. */
    val hasImages: Boolean get() = imageUrls.isNotEmpty()
}

/** 메뉴 카드 한 장. 가격은 "13000원"/"변동" 등 표시 문자열 그대로 받는다. */
data class MenuUiItem(
    val name: String,
    val priceLabel: String,
    val imageUrl: String?,
)

/** 장소 상세 DTO → 화면 모델. */
fun PlaceDetailDto.toUiModel(): PlaceDetailUiModel = PlaceDetailUiModel(
    placeId = placeId,
    name = name,
    categoryLabel = listOfNotNull(placeCategory.name, subCategory?.takeIf { it.isNotBlank() })
        .joinToString(" · "),
    address = roadAddress?.takeIf { it.isNotBlank() } ?: address,
    phone = phone,
    imageUrls = imageUrls.ifEmpty { listOfNotNull(defaultImageUrl?.takeIf { it.isNotBlank() }) },
    interiorImageUrls = interiorImageUrls,
    recommendReason = defaultRecommendReason?.takeIf { it.isNotBlank() },
    // 음식 카테고리 + 분위기 태그를 해시태그로. (중복 없이 순서 유지)
    hashTags = (foodCategories.map { it.name } + moodTags.map { it.name })
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .map { "#$it" },
    openStatus = businessStatus?.toOpenStatusLabel(),
    lastOrderText = lastOrderTime?.takeIf { it.isNotBlank() }?.let { "${it}에 라스트 오더" },
    menus = menus.map { it.toUiItem() },
)

/** 서버가 계산해 내려주는 영업 상태(OPEN/CLOSED) → 화면 라벨. 알 수 없는 값이면 표시하지 않는다. */
private fun String.toOpenStatusLabel(): String? = when (this) {
    BUSINESS_STATUS_OPEN -> "영업중"
    BUSINESS_STATUS_CLOSED -> "영업종료"
    else -> null
}

/** 메뉴 DTO → 화면 아이템. 가격이 null 이면 가격 변동 메뉴라 "변동"으로 표시한다. */
private fun PlaceMenuDto.toUiItem(): MenuUiItem = MenuUiItem(
    name = name,
    priceLabel = price?.let { "${PRICE_FORMAT.format(it)}원" } ?: "변동",
    imageUrl = imageUrl,
)

private const val BUSINESS_STATUS_OPEN = "OPEN"
private const val BUSINESS_STATUS_CLOSED = "CLOSED"
private val PRICE_FORMAT = DecimalFormat("#,###")
