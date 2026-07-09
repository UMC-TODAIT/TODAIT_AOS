package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.SearchResultDto
import com.umc.todait.feature.course.data.service.SearchService
import javax.inject.Inject

/**
 * 검색 화면 데이터 접근 계층.
 * Service 호출을 safeApiCall 로 감싸 ViewModel 에는 ApiResult 만 노출한다.
 *
 * 생성자에 @Inject 를 달아 Hilt 가 SearchService 를 주입한다. (CourseModule 참고)
 */
class SearchRepository @Inject constructor(
    private val searchService: SearchService,
) {

    /** 장소명 검색 */
    suspend fun searchPlacesByName(
        keyword: String,
        areaId: Long? = null,
        placeCategoryId: String? = null,
        page: Int? = null,
        size: Int? = null,
    ): ApiResult<SearchResultDto> = safeApiCall {
        searchService.searchPlacesByName(
            keyword = keyword,
            areaId = areaId,
            placeCategoryId = placeCategoryId,
            page = page,
            size = size,
        )
    }

    /** 검색 결과 목록(필터·정렬·페이징) */
    suspend fun searchPlaceResults(
        keyword: String,
        areaId: Long? = null,
        placeCategoryId: String? = null,
        moodTagId: Long? = null,
        foodCategoryId: Long? = null,
        sort: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        page: Int? = null,
        size: Int? = null,
    ): ApiResult<SearchResultDto> = safeApiCall {
        searchService.searchPlaceResults(
            keyword = keyword,
            areaId = areaId,
            placeCategoryId = placeCategoryId,
            moodTagId = moodTagId,
            foodCategoryId = foodCategoryId,
            sort = sort,
            latitude = latitude,
            longitude = longitude,
            page = page,
            size = size,
        )
    }
}
