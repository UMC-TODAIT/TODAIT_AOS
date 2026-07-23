package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.PlaceSearchResponseDto
import com.umc.todait.feature.course.data.service.SearchService
import javax.inject.Inject

/**
 * 장소 검색 데이터 접근 계층.
 * Service 호출을 safeApiCall 로 감싸 ViewModel 에는 ApiResult 만 노출한다.
 *
 * 생성자에 @Inject 를 달아 Hilt 가 SearchService 를 주입한다. (CourseModule 참고)
 */
class SearchRepository @Inject constructor(
    private val searchService: SearchService,
) {

    /** 장소명 검색 (GET /api/places/search?keyword=) */
    suspend fun searchPlacesByName(
        keyword: String,
    ): ApiResult<PlaceSearchResponseDto> = safeApiCall {
        searchService.searchPlacesByName(keyword = keyword)
    }
}
