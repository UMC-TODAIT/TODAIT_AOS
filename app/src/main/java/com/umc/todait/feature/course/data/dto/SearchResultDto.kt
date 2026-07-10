package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 검색 API의 result 래퍼. (BaseResponse<SearchResultDto> 형태로 내려온다.)
 *
 * "장소명 검색"과 "검색 결과 목록"이 거의 동일한 구조라 하나로 통합해 사용한다.
 * (명세서 비고: "장소명 검색과 통합 가능") hasNext 는 "검색 결과 목록"에만 존재하므로 nullable.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준.
 */
data class SearchResultDto(
    @SerializedName("keyword") val keyword: String,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("hasNext") val hasNext: Boolean? = null,
    // 검색 결과가 없으면 빈 배열([])로 내려온다.
    @SerializedName("places") val places: List<PlaceDto>,
)
