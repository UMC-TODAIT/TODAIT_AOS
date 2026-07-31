package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "분위기 태그 조회"(GET /api/mood-tags)의 result.
 *
 * 활성(is_active = true) 분위기 태그만 내려오며, 없으면 오류가 아니라 빈 배열이다.
 * 여기서 받은 [MoodTagDto.moodTagId] 가 분위기 선택 저장 요청의 moodTagIds 로 쓰인다.
 */
data class MoodTagListResponseDto(
    @SerializedName("moodTags") val moodTags: List<MoodTagDto>,
)

/**
 * 분위기 태그 1건. [MoodTagSummaryDto] 와 달리 기준 데이터 조회라 description·sortOrder 까지 내려온다.
 * [sortOrder] 는 화면 표시 순서이며 추천 순위가 아니다.
 */
data class MoodTagDto(
    @SerializedName("moodTagId") val moodTagId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("sortOrder") val sortOrder: Int,
)

/** "음식 카테고리 조회"(GET /api/food-categories)의 result. 구조는 분위기 태그와 동일하다. */
data class FoodCategoryListResponseDto(
    @SerializedName("foodCategories") val foodCategories: List<FoodCategoryDto>,
)

/** 음식 카테고리 1건. */
data class FoodCategoryDto(
    @SerializedName("foodCategoryId") val foodCategoryId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("sortOrder") val sortOrder: Int,
)
