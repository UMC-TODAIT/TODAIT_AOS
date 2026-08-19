package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.PlaceSearchResultDto
import com.umc.todait.feature.course.data.mock.MockCourse
import com.umc.todait.feature.course.data.mock.USE_COURSE_MOCK
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

    /**
     * 기준 장소 검색 (GET /api/places/search?query=&cursor=&size=)
     *
     * [cursor] 는 첫 페이지에서 null 로 두고(서버가 1로 취급), 이후에는 직전 응답의
     * nextCursor 를 그대로 넘긴다. [size] 는 한 검색어를 이어서 조회하는 동안 고정한다.
     */
    suspend fun searchPlaces(
        query: String,
        cursor: Int? = null,
        size: Int = DEFAULT_SEARCH_PAGE_SIZE,
    ): ApiResult<PlaceSearchResultDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.searchResult(query))
        return safeApiCall { searchService.searchPlaces(query = query, cursor = cursor, size = size) }
    }

    companion object {
        /** 명세 기본값. 1~15 범위 안에서만 바꿀 수 있다. */
        const val DEFAULT_SEARCH_PAGE_SIZE = 10
    }
}
