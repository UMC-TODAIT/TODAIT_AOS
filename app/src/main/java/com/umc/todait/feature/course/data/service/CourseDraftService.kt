package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.CourseDraftCreateResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftFoodCategorySaveResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftMoodTagSaveResponseDto
import com.umc.todait.feature.course.data.dto.FoodCategorySaveRequestDto
import com.umc.todait.feature.course.data.dto.MoodTagSaveRequestDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * 임시 코스(course-draft) Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 AuthInterceptor 에서 일괄 부착된다.
 *
 * ⚠️ 기준/선택 장소 저장·조회·순서변경·최종 저장 엔드포인트는 BE 미배포(보류)라
 * 배포 확정 시 이 서비스에 추가한다.
 */
interface CourseDraftService {

    /**
     * 임시 코스 생성 — POST /api/course-drafts
     * 코스 생성 진입 시 호출해 courseDraftId 를 발급받는다. 요청 바디 없음.
     */
    @POST("api/course-drafts")
    suspend fun createCourseDraft(): BaseResponse<CourseDraftCreateResponseDto>

    /**
     * 분위기 태그 선택 저장 — PUT /api/course-drafts/{courseDraftId}/mood-tags
     * 선택값 전체를 교체 저장한다(최소 2개~최대 6개).
     */
    @PUT("api/course-drafts/{courseDraftId}/mood-tags")
    suspend fun saveMoodTags(
        @Path("courseDraftId") courseDraftId: Long,
        @Body request: MoodTagSaveRequestDto,
    ): BaseResponse<CourseDraftMoodTagSaveResponseDto>

    /**
     * 음식 카테고리 선택 저장 — PUT /api/course-drafts/{courseDraftId}/food-categories
     * 선택값 전체를 교체 저장한다(최소 1개).
     */
    @PUT("api/course-drafts/{courseDraftId}/food-categories")
    suspend fun saveFoodCategories(
        @Path("courseDraftId") courseDraftId: Long,
        @Body request: FoodCategorySaveRequestDto,
    ): BaseResponse<CourseDraftFoodCategorySaveResponseDto>
}
