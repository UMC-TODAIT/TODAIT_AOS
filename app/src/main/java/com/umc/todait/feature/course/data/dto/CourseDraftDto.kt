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
