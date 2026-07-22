package com.umc.todait.feature.saved

data class CourseUiModel(
    val id: Long,
    val backgroundImage: Int,
    val topImage: Int,
    val title: String,
    val date: String,
    val moodTag: String?,
    val foodTag: String?,
    val places: List<String>
)