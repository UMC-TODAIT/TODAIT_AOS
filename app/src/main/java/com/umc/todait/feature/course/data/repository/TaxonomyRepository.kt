package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.FoodCategoryListResponseDto
import com.umc.todait.feature.course.data.dto.MoodTagListResponseDto
import com.umc.todait.feature.course.data.mock.MockCourse
import com.umc.todait.feature.course.data.mock.USE_COURSE_MOCK
import com.umc.todait.feature.course.data.service.TaxonomyService
import javax.inject.Inject

/**
 * 기준 데이터(분위기 태그·음식 카테고리) 조회 계층.
 *
 * 취향 설정 화면은 여기서 받은 목록으로 선택지를 그리고, 각 항목의 id 를 저장 API 에 그대로 넘긴다.
 * (id 를 앱에 하드코딩하지 않는다 — 서버 기준 데이터가 바뀌어도 화면이 따라가야 하기 때문)
 */
class TaxonomyRepository @Inject constructor(
    private val taxonomyService: TaxonomyService,
) {

    /** 분위기 태그 목록 (GET /api/mood-tags) */
    suspend fun getMoodTags(): ApiResult<MoodTagListResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.moodTags)
        return safeApiCall { taxonomyService.getMoodTags() }
    }

    /** 음식 카테고리 목록 (GET /api/food-categories) */
    suspend fun getFoodCategories(): ApiResult<FoodCategoryListResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.foodCategories)
        return safeApiCall { taxonomyService.getFoodCategories() }
    }
}
