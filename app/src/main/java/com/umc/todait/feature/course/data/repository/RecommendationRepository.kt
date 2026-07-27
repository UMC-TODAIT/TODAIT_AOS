package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.HotPlaceResultDto
import com.umc.todait.feature.course.data.dto.RecommendedPlaceResultDto
import com.umc.todait.feature.course.data.mock.MockCourse
import com.umc.todait.feature.course.data.mock.USE_COURSE_MOCK
import com.umc.todait.feature.course.data.service.RecommendationService
import javax.inject.Inject

/**
 * 추천 장소 데이터 접근 계층.
 * Service 호출을 safeApiCall 로 감싸 ViewModel 에는 ApiResult 만 노출한다.
 *
 * 생성자에 @Inject 를 달아 Hilt 가 RecommendationService 를 주입한다. (CourseModule 참고)
 */
class RecommendationRepository @Inject constructor(
    private val recommendationService: RecommendationService,
) {

    /**
     * 지금 내 주변 핫플 조회 (기준 장소 설정 화면).
     * 위치 권한이 없으면 [latitude]·[longitude] 를 모두 생략한다(둘 중 하나만 보내면 LOCATION400).
     */
    suspend fun getHotPlaces(
        courseDraftId: Long,
        latitude: Double? = null,
        longitude: Double? = null,
        size: Int? = null,
    ): ApiResult<HotPlaceResultDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.hotPlaces)
        return safeApiCall {
            recommendationService.getHotPlaces(
                courseDraftId = courseDraftId,
                latitude = latitude,
                longitude = longitude,
                size = size,
            )
        }
    }

    /** 카테고리별 추천 장소 조회 (코스 구성하기 화면). */
    suspend fun getRecommendedPlaces(
        courseDraftId: Long,
        placeCategoryCode: String,
        size: Int? = null,
    ): ApiResult<RecommendedPlaceResultDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.recommendedPlaces(placeCategoryCode))
        return safeApiCall {
            recommendationService.getRecommendedPlaces(
                courseDraftId = courseDraftId,
                placeCategoryCode = placeCategoryCode,
                size = size,
            )
        }
    }
}
