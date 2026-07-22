package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.PlaceSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 장소 검색 Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 AuthInterceptor 에서 일괄 부착된다.
 *
 * ⚠️ 배포 스펙 기준. 이전 명세의 `/api/search/places`(검색 결과 목록, 필터·정렬·페이징)와
 * `/api/recommendations/places`(추천)는 서버에서 제거/미배포라 여기서 다루지 않는다.
 */
interface SearchService {

    /**
     * 장소명 검색 — GET /api/places/search?keyword=
     * 장소명(place.name)에 검색어가 포함된 장소 목록을 반환한다. (기준 장소 설정 화면 등에서 사용)
     */
    @GET("api/places/search")
    suspend fun searchPlacesByName(
        @Query("keyword") keyword: String,
    ): BaseResponse<PlaceSearchResponseDto>
}
