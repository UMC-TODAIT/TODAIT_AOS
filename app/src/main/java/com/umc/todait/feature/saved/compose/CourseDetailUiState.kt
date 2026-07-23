package com.umc.todait.feature.saved.compose

import com.umc.todait.feature.saved.PlaceUiModel

data class CourseDetailUiState(
    val title: String = "",
    val date: String = "",

    val moodTag: String? = null,
    val moodTagCode: String? = null,

    val foodTag: String? = null,
    val foodTagCode: String? = null,

    val memo: String = "",

    val places: List<PlaceUiModel> = emptyList(),

    val isLoading: Boolean = false
)