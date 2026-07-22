package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.PlaceCategoryListResponseDto
import com.umc.todait.feature.course.data.service.PlaceCategoryService
import javax.inject.Inject

/**
 * 장소 대분류(카테고리) 데이터 접근 계층.
 * Service 호출을 safeApiCall 로 감싸 ViewModel 에는 ApiResult 만 노출한다.
 *
 * 생성자에 @Inject 를 달아 Hilt 가 PlaceCategoryService 를 주입한다. (CourseModule 참고)
 */
class PlaceCategoryRepository @Inject constructor(
    private val placeCategoryService: PlaceCategoryService,
) {

    /** 장소 대분류 목록 (GET /api/place-categories) */
    suspend fun getPlaceCategories(): ApiResult<PlaceCategoryListResponseDto> = safeApiCall {
        placeCategoryService.getPlaceCategories()
    }
}
