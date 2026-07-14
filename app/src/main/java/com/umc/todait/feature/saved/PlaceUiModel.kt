package com.umc.todait.feature.saved

data class PlaceUiModel(
    val isStartPlace: Boolean,
    val name: String,
    val address: String,
    val backgroundImage: Int,
    var memo: String = ""
)