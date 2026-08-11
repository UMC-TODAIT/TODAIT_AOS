package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.FoodCategoryListResponseDto
import com.umc.todait.feature.course.data.dto.MoodTagListResponseDto
import com.umc.todait.feature.course.data.local.CourseTaxonomy
import com.umc.todait.feature.course.data.service.TaxonomyService
import javax.inject.Inject

/**
 * 기준 데이터(분위기 태그·음식 카테고리) 조회 계층.
 *
 * 취향 설정 화면은 여기서 받은 목록으로 선택지를 그리고, 각 항목의 id 를 저장 API 에 그대로 넘긴다.
 *
 * ⚠️ 두 조회 API 는 명세 비고에 "프론트엔드에서 담당해주시기로 합의됐습니다"라고 적힌 항목이라
 * **배포 서버에 존재하지 않는다**(호출하면 COMMON500). 그래서 기본값은 앱이 보유한 [CourseTaxonomy] 다.
 * BE 가 조회 API 를 배포하면 [USE_SERVER_TAXONOMY] 만 true 로 바꾸면 서버 응답을 타게 된다.
 */
class TaxonomyRepository @Inject constructor(
    private val taxonomyService: TaxonomyService,
) {

    /** 분위기 태그 목록 (GET /api/mood-tags — 미배포, 현재는 앱 보유 데이터) */
    suspend fun getMoodTags(): ApiResult<MoodTagListResponseDto> {
        if (!USE_SERVER_TAXONOMY) return ApiResult.Success(CourseTaxonomy.moodTags)
        return safeApiCall { taxonomyService.getMoodTags() }
    }

    /** 음식 카테고리 목록 (GET /api/food-categories — 미배포, 현재는 앱 보유 데이터) */
    suspend fun getFoodCategories(): ApiResult<FoodCategoryListResponseDto> {
        if (!USE_SERVER_TAXONOMY) return ApiResult.Success(CourseTaxonomy.foodCategories)
        return safeApiCall { taxonomyService.getFoodCategories() }
    }

    private companion object {
        // BE 가 GET /api/mood-tags · /api/food-categories 를 배포하면 true 로 바꾼다.
        const val USE_SERVER_TAXONOMY = false
    }
}
