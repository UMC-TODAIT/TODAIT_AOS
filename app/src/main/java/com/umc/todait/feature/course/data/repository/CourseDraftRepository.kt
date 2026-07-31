package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.CourseDraftCreateResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftFoodCategorySaveResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftMoodTagSaveResponseDto
import com.umc.todait.feature.course.data.dto.FoodCategorySaveRequestDto
import com.umc.todait.feature.course.data.dto.MoodTagSaveRequestDto
import com.umc.todait.feature.course.data.mock.MockCourse
import com.umc.todait.feature.course.data.mock.USE_COURSE_MOCK
import com.umc.todait.feature.course.data.service.CourseDraftService
import javax.inject.Inject

/**
 * 임시 코스(course-draft) 데이터 접근 계층.
 * Service 호출을 safeApiCall 로 감싸 ViewModel 에는 ApiResult 만 노출한다.
 *
 * 생성자에 @Inject 를 달아 Hilt 가 CourseDraftService 를 주입한다. (CourseModule 참고)
 */
class CourseDraftRepository @Inject constructor(
    private val courseDraftService: CourseDraftService,
) {

    /** 임시 코스 생성 (POST /api/course-drafts) → courseDraftId 발급 */
    suspend fun createCourseDraft(): ApiResult<CourseDraftCreateResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.courseDraft)
        return safeApiCall { courseDraftService.createCourseDraft() }
    }

    /**
     * 분위기 태그 선택 저장 (PUT /api/course-drafts/{courseDraftId}/mood-tags).
     * [moodTagIds] 는 `GET /api/mood-tags` 로 받아온 id 그대로다(2~6개, 중복 불가).
     * 부분 추가가 아니라 현재 선택값 전체를 교체 저장한다.
     */
    suspend fun saveMoodTags(
        courseDraftId: Long,
        moodTagIds: List<Long>,
    ): ApiResult<CourseDraftMoodTagSaveResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.moodTagsSaveResult(moodTagIds))
        return safeApiCall { courseDraftService.saveMoodTags(courseDraftId, MoodTagSaveRequestDto(moodTagIds)) }
    }

    /**
     * 음식 카테고리 선택 저장 (PUT /api/course-drafts/{courseDraftId}/food-categories).
     * [foodCategoryIds] 는 `GET /api/food-categories` 로 받아온 id 그대로다(1개 이상).
     * 분위기 태그와 마찬가지로 선택값 전체를 교체 저장한다.
     */
    suspend fun saveFoodCategories(
        courseDraftId: Long,
        foodCategoryIds: List<Long>,
    ): ApiResult<CourseDraftFoodCategorySaveResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.foodCategoriesSaveResult(foodCategoryIds))
        return safeApiCall {
            courseDraftService.saveFoodCategories(courseDraftId, FoodCategorySaveRequestDto(foodCategoryIds))
        }
    }
}
