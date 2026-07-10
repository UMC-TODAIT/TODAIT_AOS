package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.RecommendationResultDto
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

    /** 추천 장소 조회 */
    suspend fun getRecommendedPlaces(
        type: String,
        courseDraftId: Long? = null,
        basePlaceId: Long? = null,
        placeCategoryId: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        size: Int? = null,
    ): ApiResult<RecommendationResultDto> = safeApiCall {
        recommendationService.getRecommendedPlaces(
            type = type,
            courseDraftId = courseDraftId,
            basePlaceId = basePlaceId,
            placeCategoryId = placeCategoryId,
            latitude = latitude,
            longitude = longitude,
            size = size,
        )
    }
}
