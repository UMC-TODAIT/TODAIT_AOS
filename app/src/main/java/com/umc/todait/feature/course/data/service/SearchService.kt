package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.SearchResultDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 기준 장소 설정 - 검색 화면 관련 Retrofit 서비스.
 *
 * 두 엔드포인트는 필드가 대부분 겹쳐(BE에서도 "통합 가능"으로 표기) 하나의 Service로 묶는다.
 * 모든 API는 로그인 필요(Authorization: Bearer). 토큰 헤더는 추후 AuthInterceptor 에서 일괄 부착 예정.
 */
interface SearchService {

    /**
     * 장소명 검색 — GET /api/places/search
     * 장소명(name)에 검색어가 포함된 장소 목록을 반환한다.
     */
    @GET("api/places/search")
    suspend fun searchPlacesByName(
        @Query("keyword") keyword: String,
        @Query("areaId") areaId: Long? = null,
        @Query("placeCategoryId") placeCategoryId: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): BaseResponse<SearchResultDto>

    /**
     * 검색 결과 목록 — GET /api/search/places
     * 카드 정보(태그·저장 수 등)와 필터·정렬·페이징을 지원한다.
     * sort: RELEVANCE(기본) / POPULAR / DISTANCE. DISTANCE 정렬 시 latitude·longitude 필수.
     *
     * TODO: 명세서 Path Variable 에 courseId 가 적혀 있으나 uri(/api/search/places)에는
     *       경로 변수가 없다. BE에 확인 후 필요하면 @Path 로 반영한다.
     */
    @GET("api/search/places")
    suspend fun searchPlaceResults(
        @Query("keyword") keyword: String,
        @Query("areaId") areaId: Long? = null,
        @Query("placeCategoryId") placeCategoryId: String? = null,
        @Query("moodTagId") moodTagId: Long? = null,
        @Query("foodCategoryId") foodCategoryId: Long? = null,
        @Query("sort") sort: String? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): BaseResponse<SearchResultDto>
}
