package com.umc.todait.feature.home.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "추천 코스 목록 조회"(GET /api/recommended-courses) 의 result.
 * 홈 화면은 page=0, size=3 사용 — 서버가 날짜 기준으로 홍대/연남/성수 코스를 순환 반환한다.
 */
data class RecommendedCourseListResultDto(
    @SerializedName("recommendationLogId") val recommendationLogId: Long,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("courses") val courses: List<RecommendedCourseSummaryDto>,
)

/** "오늘의 추천 코스" 카드 한 장(목록 조회 응답 원소). */
data class RecommendedCourseSummaryDto(
    @SerializedName("courseId") val courseId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("area") val area: HomeAreaDto,
    @SerializedName("representativeImageUrl") val representativeImageUrl: String?,
    @SerializedName("tags") val tags: List<CourseTagDto>,
    @SerializedName("placeCount") val placeCount: Int,
    @SerializedName("rank") val rank: Int,
    @SerializedName("detailAvailable") val detailAvailable: Boolean,
)

/** 목록 카드의 대표 태그 1건(첫 번째=MOOD, 두 번째=SUB_CATEGORY). */
data class CourseTagDto(
    @SerializedName("type") val type: String,
    @SerializedName("code") val code: String?,
    @SerializedName("name") val name: String,
)

/**
 * "추천 코스 상세 조회"(GET /api/recommended-courses/{courseId}) 의 result.
 * 홈 "오늘의 추천 코스" 카드 탭 → 추천 코스 상세 화면(#55) 진입 시 사용. 목록과 달리 대표 태그를
 * tags 배열이 아닌 flat 필드(representativeMoodTag/representativePlaceCategory)로 받는다 — API 명세 원문 기준.
 */
data class RecommendedCourseDetailDto(
    @SerializedName("courseId") val courseId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("representativeMoodTag") val representativeMoodTag: MoodTagDto?,
    @SerializedName("representativePlaceCategory") val representativePlaceCategory: PlaceCategoryDto?,
    @SerializedName("placeCount") val placeCount: Int,
    @SerializedName("places") val places: List<RecommendedCoursePlaceDto>,
)

data class MoodTagDto(
    @SerializedName("moodTagId") val moodTagId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)

data class PlaceCategoryDto(
    @SerializedName("placeCategoryId") val placeCategoryId: Long? = null,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)

data class RecommendedCoursePlaceDto(
    @SerializedName("coursePlaceId") val coursePlaceId: Long,
    @SerializedName("placeId") val placeId: Long?,
    @SerializedName("visitOrder") val visitOrder: Int,
    @SerializedName("name") val name: String,
    @SerializedName("representativeImageUrl") val representativeImageUrl: String?,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
)

/** "추천 코스 저장"(POST /api/recommended-courses/{courseId}/save) 의 result. */
data class RecommendedCourseSaveResultDto(
    @SerializedName("sourceCourseId") val sourceCourseId: Long,
    @SerializedName("savedCourseId") val savedCourseId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("visibility") val visibility: String,
    @SerializedName("sourceType") val sourceType: String,
    @SerializedName("placeCount") val placeCount: Int,
    @SerializedName("savedAt") val savedAt: String,
)
