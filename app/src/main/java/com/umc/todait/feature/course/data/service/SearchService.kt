package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.PlaceSearchResultDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 장소 검색 Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 AuthInterceptor 에서 일괄 부착된다.
 */
interface SearchService {

    /**
     * 기준 장소 검색 — GET /api/places/search?query=&cursor=&size=
     *
     * 서버가 카카오 Local API 로 키워드 검색한 뒤 지원 지역·카테고리 필터를 적용해 반환한다.
     * 정확한 장소명 검색이 아니라 관련도 기반 키워드 검색이며, 사용자 현재 위치는 전달하지 않는다.
     *
     * [query] 는 앞뒤 공백을 제거한 뒤 2자 이상이어야 한다(미만이면 PLACE_SEARCH400).
     *
     * 커서 기반 페이지네이션이다.
     * - [cursor]: 1~45. 첫 요청에서는 null(생략)로 두고, 이후에는 직전 응답의 nextCursor 를 그대로 넘긴다.
     * - [size]: 1~15(기본 10). 같은 검색어를 이어서 조회하는 동안은 동일한 값을 유지한다.
     */
    @GET("api/places/search")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("cursor") cursor: Int?,
        @Query("size") size: Int?,
    ): BaseResponse<PlaceSearchResultDto>
}
