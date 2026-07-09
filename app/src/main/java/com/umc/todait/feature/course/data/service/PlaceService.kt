package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.PlaceDetailDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 장소 상세(카드) 조회 Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 추후 AuthInterceptor 에서 일괄 부착 예정.
 */
interface PlaceService {

    /**
     * 장소 카드 정보 — GET /api/places/{placeId}
     * 특정 장소의 카드 상세(기본 정보·이미지·분위기 태그·음식 카테고리)를 반환한다.
     * exposure_status = ACTIVE 이고 deleted_at 이 없는 장소만 조회 가능(그 외 PLACE404).
     */
    @GET("api/places/{placeId}")
    suspend fun getPlaceDetail(
        @Path("placeId") placeId: Long,
    ): BaseResponse<PlaceDetailDto>
}
