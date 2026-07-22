package com.umc.todait.feature.saved

data class PlaceUiModel(
    val isStartPlace: Boolean,
    val name: String,
    val address: String,
    var backgroundImage: Int,
    var memo: String = ""
)