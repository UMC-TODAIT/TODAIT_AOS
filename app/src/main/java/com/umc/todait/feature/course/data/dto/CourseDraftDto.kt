package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 분위기 태그 선택 저장 요청(PUT /api/course-drafts/{courseDraftId}/mood-tags).
 * 최소 2개~최대 6개. 이미 저장된 값 전체를 교체한다(부분 추가 아님).
 */
data class MoodTagSaveRequestDto(
    @SerializedName("moodTagIds") val moodTagIds: List<Long>,
)

/**
 * 분위기 태그 선택 저장 result. 최초 저장 시 draftStatus 가 FOOD_SELECTING 으로 전이한다.
 */
data class CourseDraftMoodTagSaveResponseDto(
    @SerializedName("courseDraftId") val courseDraftId: Long,
    @SerializedName("draftStatus") val draftStatus: String?,
    @SerializedName("moodTags") val moodTags: List<MoodTagSummaryDto>,
)

/**
 * 음식 카테고리 선택 저장 요청(PUT /api/course-drafts/{courseDraftId}/food-categories).
 * 최소 1개. 이미 저장된 값 전체를 교체한다(부분 추가 아님).
 */
data class FoodCategorySaveRequestDto(
    @SerializedName("foodCategoryIds") val foodCategoryIds: List<Long>,
)

/**
 * 음식 카테고리 선택 저장 result. 최초 저장 시 draftStatus 가 BASE_PLACE_SELECTING 으로 전이한다.
 */
data class CourseDraftFoodCategorySaveResponseDto(
    @SerializedName("courseDraftId") val courseDraftId: Long,
    @SerializedName("draftStatus") val draftStatus: String?,
    @SerializedName("foodCategories") val foodCategories: List<FoodCategorySummaryDto>,
)

/**
 * "임시 코스 생성"(POST /api/course-drafts)의 result. (BaseResponse<CourseDraftCreateResponseDto>)
 *
 * 코스 생성 진입 시 발급되는 임시 코스(course-draft) 핸들. 이후 기준/선택 장소 저장·순서 변경·
 * 최종 저장 API 가 모두 이 [courseDraftId] 를 경로 변수로 사용한다.
 *
 * 요청 바디는 없다(로그인 사용자 기준으로 서버가 생성).
 */
data class CourseDraftCreateResponseDto(
    @SerializedName("courseDraftId") val courseDraftId: Long,
    // 생성 직후 정상값은 MOOD_SELECTING.
    // (DB/Entity 는 status 지만 API JSON 은 임시 코스 상태임을 분명히 하려고 draftStatus 를 쓴다.)
    // 배포 서버가 아직 구버전 필드명(status)을 내려주고 있어 alternate 로 함께 받는다.
    @SerializedName(value = "draftStatus", alternate = ["status"]) val draftStatus: String?,
    // 만료 정책 미확정 — 명세상 응답에서 빠지거나 null 로 올 수 있다.
    @SerializedName("expiresAt") val expiresAt: String?,
    // ISO-8601 LocalDateTime.
    @SerializedName("createdAt") val createdAt: String,
)

/**
 * "임시 코스 기준 장소 설정"(PATCH /api/course-drafts/{courseDraftId}/base-place)의 요청 바디.
 *
 * 명세상 [placeId] 와 [externalPlace] 중 **정확히 하나만** 채워야 한다(둘 다 있거나 둘 다 없으면 COURSE400).
 * - 주변 핫플/운영자 등록 장소, 이미 내부 DB 에 있는 카카오 검색 장소 → [placeId]
 * - 내부 DB 미등록 카카오 검색 장소(검색 응답의 placeId 가 null) → [externalPlace]
 */
data class BasePlaceSetRequestDto(
    @SerializedName("placeId") val placeId: Long? = null,
    @SerializedName("externalPlace") val externalPlace: ExternalPlaceDto? = null,
)

/**
 * 내부 DB 미등록 카카오 장소 정보. 서버가 (dataSourceCode + sourcePlaceId) 로 기존 장소를 찾고,
 * 없으면 새 place 를 만든다.
 *
 * ⚠️ [areaCode]·[categoryCode] 는 앱이 주소/카테고리를 직접 변환한 값이 아니라
 * **장소 검색 API 응답이 내려준 코드를 그대로** 전달한다(명세 "프론트엔드가 직접 변환하지 않습니다").
 */
data class ExternalPlaceDto(
    // MVP 허용값은 KAKAO 뿐이다.
    @SerializedName("dataSourceCode") val dataSourceCode: String,
    // 카카오가 부여한 장소 고유 ID(검색 응답의 externalPlaceId).
    @SerializedName("sourcePlaceId") val sourcePlaceId: String,
    @SerializedName("name") val name: String,
    // 지번 주소(필수).
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    // HONGDAE / YEONNAM / SEONGSU.
    @SerializedName("areaCode") val areaCode: String,
    // CAFE / RESTAURANT / ACTIVITY / BAR.
    @SerializedName("categoryCode") val categoryCode: String,
    @SerializedName("subCategory") val subCategory: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("sourceUrl") val sourceUrl: String? = null,
)

/**
 * 기준 장소 설정 result. 성공 시 임시 코스 상태가 BASE_PLACE_SELECTING → PLACE_SELECTING 으로 전이하고,
 * course_draft_place 에도 visit_order = 1 / place_role = BASE 로 함께 저장된다.
 */
data class BasePlaceSetResponseDto(
    @SerializedName("courseDraftId") val courseDraftId: Long,
    // 명세 응답 필드명은 status. 서버가 draftStatus 로 내려주는 경우도 있어 alternate 로 함께 받는다.
    @SerializedName(value = "status", alternate = ["draftStatus"]) val status: String?,
    @SerializedName("basePlace") val basePlace: BasePlaceResultDto,
)

/**
 * 최종 확정된 기준 장소. 카카오 검색 장소를 새로 등록한 경우에도 **서버가 만든 내부 place.id** 가 내려온다.
 */
data class BasePlaceResultDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("area") val area: AreaSummaryDto,
    @SerializedName("category") val category: PlaceCategorySummaryDto,
    @SerializedName("subCategory") val subCategory: String?,
    // 대표 데이터 출처. OPERATOR / KAKAO.
    @SerializedName("sourceType") val sourceType: String?,
    // 이번 요청에서 place 가 새로 생성됐는지 여부.
    @SerializedName("isNewPlace") val isNewPlace: Boolean?,
)

/**
 * 임시 코스에 담긴 장소 한 건.
 * 순서 설정 화면 진입 / 순서 변경 / 저장 화면 진입 응답이 모두 같은 모양을 쓴다.
 *
 * [draggable]·[deletable] 은 순서 설정 화면 진입 응답에만 있고 나머지 응답에는 없어 nullable 이다.
 * (없을 때는 [placeRole] 로 판단한다 — BASE 는 고정, SELECTED 만 드래그 가능.)
 */
data class CourseDraftPlaceDto(
    // 임시 코스와 장소의 연결 행 ID. 순서 변경 요청에 쓰는 값은 placeId 가 아니라 이 값이다.
    @SerializedName("courseDraftPlaceId") val courseDraftPlaceId: Long,
    @SerializedName("placeId") val placeId: Long,
    // BASE(기준 장소, visitOrder 1 고정) / SELECTED(사용자가 담은 장소, visitOrder 2부터).
    @SerializedName("placeRole") val placeRole: String,
    @SerializedName("visitOrder") val visitOrder: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String? = null,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("draggable") val draggable: Boolean? = null,
    @SerializedName("deletable") val deletable: Boolean? = null,
) {
    /** 기준 장소 여부. 지도 핀 1번·드래그 불가 판단에 쓴다. */
    val isBase: Boolean get() = placeRole == PLACE_ROLE_BASE

    companion object {
        const val PLACE_ROLE_BASE = "BASE"
    }
}

/**
 * "임시 코스 순서 설정 화면 진입"(PATCH /api/course-drafts/{courseDraftId}/ordering)의 result.
 *
 * 요청 바디는 없다. PLACE_SELECTING → ORDERING 으로 전이하며, 이미 ORDERING 이면 상태를 바꾸지 않고
 * 같은 성공 응답을 준다(멱등). 화면 중복 마운트/재시도로 다시 호출돼도 응답 [places] 를 최신 데이터로 쓰면 된다.
 */
data class OrderingEntryResponseDto(
    @SerializedName("courseDraftId") val courseDraftId: Long,
    // 정상값 ORDERING.
    @SerializedName(value = "draftStatus", alternate = ["status"]) val draftStatus: String?,
    // 기준 장소를 포함한 전체 장소 수.
    @SerializedName("totalPlaceCount") val totalPlaceCount: Int?,
    // 기준 장소를 제외한 SELECTED 장소 수.
    @SerializedName("selectedPlaceCount") val selectedPlaceCount: Int?,
    // visitOrder 오름차순.
    @SerializedName("places") val places: List<CourseDraftPlaceDto>,
)

/**
 * "선택 장소 순서 변경"(PATCH /api/course-drafts/{courseDraftId}/places/order)의 요청 바디.
 *
 * ⚠️ 기준 장소(BASE)는 서버가 항상 visitOrder = 1 로 유지하므로 **목록에 넣지 않는다**.
 * 드래그 중간 과정이 아니라 **선택 장소 전체의 최종 순서**를 한 번에 보낸다.
 */
data class PlaceOrderUpdateRequestDto(
    @SerializedName("placeOrders") val placeOrders: List<PlaceOrderItemDto>,
)

/** 순서 변경 항목. [visitOrder] 는 기준 장소가 1번이므로 2부터 중복 없이 연속해야 한다. */
data class PlaceOrderItemDto(
    @SerializedName("courseDraftPlaceId") val courseDraftPlaceId: Long,
    @SerializedName("visitOrder") val visitOrder: Int,
)

/** 순서 변경 result. [places] 는 기준 장소를 포함한 전체 목록을 visitOrder 오름차순으로 준다. */
data class PlaceOrderUpdateResponseDto(
    @SerializedName("courseDraftId") val courseDraftId: Long,
    @SerializedName("places") val places: List<CourseDraftPlaceDto>,
)

/**
 * "임시 코스 저장 화면 진입"(PATCH /api/course-drafts/{courseDraftId}/saving)의 result.
 *
 * 요청 바디는 없다. ORDERING → SAVING 으로 전이하며 ordering 과 마찬가지로 멱등이다.
 * 이 시점에는 아직 최종 course 를 만들지 않는다(코스 저장 요청 API 에서 생성).
 * 코스 이름·메모·태그는 화면 입력값이라 요청에도 응답에도 없다.
 */
data class CourseDraftSavingEnterResponseDto(
    @SerializedName("courseDraftId") val courseDraftId: Long,
    // 정상값 SAVING.
    @SerializedName(value = "draftStatus", alternate = ["status"]) val draftStatus: String?,
    @SerializedName("totalPlaceCount") val totalPlaceCount: Int?,
    // 코스 저장 화면 '경로 미리보기' 영역에 그대로 표시할 장소 목록(visitOrder 오름차순).
    @SerializedName("routePreview") val routePreview: List<CourseDraftPlaceDto>,
)
