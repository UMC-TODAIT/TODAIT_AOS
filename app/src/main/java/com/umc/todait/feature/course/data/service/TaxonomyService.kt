package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.FoodCategoryListResponseDto
import com.umc.todait.feature.course.data.dto.MoodTagListResponseDto
import retrofit2.http.GET

/**
 * 기준 데이터(taxonomy) Retrofit 서비스 — 분위기 태그·음식 카테고리 목록 조회.
 *
 * 취향 설정 화면(분위기/음식 선택)이 선택지를 구성하고, 저장 API 에 넘길 id 를 확보하는 용도다.
 * 목록이 비어 있어도 오류가 아니라 빈 배열로 내려온다.
 */
interface TaxonomyService {

    /** 분위기 태그 목록 조회 — GET /api/mood-tags */
    @GET("api/mood-tags")
    suspend fun getMoodTags(): BaseResponse<MoodTagListResponseDto>

    /** 음식 카테고리 목록 조회 — GET /api/food-categories */
    @GET("api/food-categories")
    suspend fun getFoodCategories(): BaseResponse<FoodCategoryListResponseDto>
}
