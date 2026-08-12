package com.umc.todait.feature.saved

data class PlaceUiModel(
    val placeId: Long?,
    val name: String,
    val address: String,
    val imageUrl: String? = null
)
