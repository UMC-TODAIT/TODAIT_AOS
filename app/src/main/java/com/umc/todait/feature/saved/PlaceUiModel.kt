package com.umc.todait.feature.saved

data class PlaceUiModel(
    val coursePlaceId: Long,
    val placeId: Long,
    val isStartPlace: Boolean,
    val name: String,
    val address: String,
    val imageUrl: String? = null,
    var backgroundImage: Int,
    var memo: String = ""
)