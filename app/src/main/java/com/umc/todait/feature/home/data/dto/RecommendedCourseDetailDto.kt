package com.umc.todait.feature.home.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "추천 코스 상세 조회"(GET /api/recommended-courses/{courseId}) 의 result.
 *
 * ⚠️ TODAIT_BE 스펙 확정본(2026-07-22) 기준. 홈 "오늘의 추천 코스" 카드 탭 → 상세 화면 진입 시 사용.
 * 목록 조회 API 는 아직 명세가 없어 courseId 는 당장 DUMMY_COURSES 로만 테스트 가능하다.
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
