package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.PlaceDetailDto
import com.umc.todait.feature.course.data.service.PlaceService
import javax.inject.Inject

/**
 * 장소 상세(카드) 데이터 접근 계층.
 * Service 호출을 safeApiCall 로 감싸 ViewModel 에는 ApiResult 만 노출한다.
 *
 * 생성자에 @Inject 를 달아 Hilt 가 PlaceService 를 주입한다. (CourseModule 참고)
 */
class PlaceRepository @Inject constructor(
    private val placeService: PlaceService,
) {

    /** 장소 카드 정보 */
    suspend fun getPlaceDetail(
        placeId: Long,
    ): ApiResult<PlaceDetailDto> = safeApiCall {
        placeService.getPlaceDetail(placeId = placeId)
    }
}
