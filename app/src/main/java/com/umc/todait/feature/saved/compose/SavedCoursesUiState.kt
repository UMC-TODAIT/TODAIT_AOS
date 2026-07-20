package com.umc.todait.feature.saved.compose

import com.umc.todait.feature.saved.CourseUiModel

data class SavedCoursesUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val recentCourses: List<CourseUiModel> = emptyList(),
    val frequentlyViewedCourses: List<CourseUiModel> = emptyList(),
)