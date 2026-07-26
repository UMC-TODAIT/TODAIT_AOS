package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.PlaceSearchResultDto
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

    /** 기준 장소 검색 (GET /api/places/search?query=) */
    suspend fun searchPlaces(
        query: String,
    ): ApiResult<PlaceSearchResultDto> = safeApiCall {
        searchService.searchPlaces(query = query)
    }
}
