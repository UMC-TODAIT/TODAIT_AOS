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
     * 기준 장소 검색 — GET /api/places/search?query=
     *
     * 서버가 카카오 Local API 로 키워드 검색한 뒤 지원 지역·카테고리 필터를 적용해 최대 10개를 반환한다.
     * 정확한 장소명 검색이 아니라 관련도 기반 키워드 검색이며, 사용자 현재 위치는 전달하지 않는다.
     *
     * [query] 는 앞뒤 공백을 제거한 뒤 2자 이상이어야 한다(미만이면 PLACE_SEARCH400).
     */
    @GET("api/places/search")
    suspend fun searchPlaces(
        @Query("query") query: String,
    ): BaseResponse<PlaceSearchResultDto>
}
