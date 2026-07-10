package com.umc.todait.feature.saved

data class CourseUiModel(
    val backgroundImage: Int,
    val topImage: Int,
    val title: String,
    val date: String,
    val moodTag: Int,
    val foodTag: Int,
    val places: List<String>
)