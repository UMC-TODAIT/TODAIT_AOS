package com.umc.todait.feature.home.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "추천 코스 목록 조회"(GET /api/recommended-courses) 의 result.
 * 홈 화면은 page=0, size=3 사용 — 서버가 날짜 기준으로 홍대/연남/성수 코스를 순환 반환한다.
 */
data class RecommendedCourseListResultDto(
    @SerializedName("courses") val courses: List<RecommendedCourseSummaryDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
)

/** "오늘의 추천 코스" 카드 한 장(목록 조회 응답 원소). */
data class RecommendedCourseSummaryDto(
    @SerializedName("courseId") val courseId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("representativeImageUrl") val representativeImageUrl: String?,
    @SerializedName("representativeMoodTag") val representativeMoodTag: MoodTagDto?,
    @SerializedName("representativePlaceCategory") val representativePlaceCategory: PlaceCategoryDto?,
)

/**
 * "추천 코스 상세 조회"(GET /api/recommended-courses/{courseId}) 의 result.
 *
 * ⚠️ TODAIT_BE 스펙 확정본(2026-07-22) 기준. 홈 "오늘의 추천 코스" 카드 탭 → 상세 화면 진입 시 사용.
 * (상세/저장 화면 연결은 아직 안 함 — feature/saved 소유 CourseDetailScreen 조율 후 진행)
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
