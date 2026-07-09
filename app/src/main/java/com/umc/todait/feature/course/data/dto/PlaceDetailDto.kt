package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "장소 카드 정보"(GET /api/places/{placeId})의 result.
 *
 * 장소 카드에 표시할 상세 정보 — 기본 정보, 대표/추가 이미지, 분위기 태그, 음식 카테고리 등.
 * (BaseResponse<PlaceDetailDto> 형태로 내려온다.)
 *
 * 검색 카드용 PlaceDto 와 필드가 겹치지만, 상세 화면에서만 쓰는 필드(subCategory, roadAddress,
 * phone, images, foodCategories, recommendReason, selectedCount 등)가 많아 별도 DTO 로 둔다.
 * internal_score / popularity_score 등 내부 지표는 응답에 노출되지 않는다.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class PlaceDetailDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("subCategory") val subCategory: String?,
    @SerializedName("areaName") val areaName: String,
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("phone") val phone: String?,
    @SerializedName("defaultImageUrl") val defaultImageUrl: String?,
    // place_image.display_order 오름차순 정렬. 이미지가 없으면 빈 배열([])로 내려온다.
    @SerializedName("images") val images: List<PlaceImageDto>,
    // place_mood_tag 중 is_confirmed = true 인 태그만 내려온다.
    @SerializedName("moodTags") val moodTags: List<String>,
    @SerializedName("foodCategories") val foodCategories: List<String>,
    @SerializedName("recommendReason") val recommendReason: String?,
    @SerializedName("savedCount") val savedCount: Int,
    @SerializedName("selectedCount") val selectedCount: Int,
)

/**
 * 장소 상세 이미지 한 장. displayOrder 오름차순으로 정렬돼 내려온다.
 */
data class PlaceImageDto(
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("displayOrder") val displayOrder: Int,
)
