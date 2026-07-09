package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 검색 결과 카드 한 장에 해당하는 장소 정보.
 *
 * "장소명 검색"(GET /api/places/search)과 "검색 결과 목록"(GET /api/search/places)이
 * 공통으로 사용하며, moodTags / savedCount / distanceMeters 는 "검색 결과 목록"에서만
 * 내려오므로 nullable 로 둔다.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class PlaceDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("areaName") val areaName: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("imageUrl") val imageUrl: String?,
    // 아래 3개는 "검색 결과 목록"에서만 포함된다.
    @SerializedName("moodTags") val moodTags: List<String>? = null,
    @SerializedName("savedCount") val savedCount: Int? = null,
    // latitude/longitude 파라미터가 있을 때만 계산돼 내려오며, 없으면 null.
    @SerializedName("distanceMeters") val distanceMeters: Int? = null,
)
