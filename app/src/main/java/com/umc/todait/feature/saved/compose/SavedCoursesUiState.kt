package com.umc.todait.feature.saved.compose

import com.umc.todait.core.network.UiError
import com.umc.todait.feature.saved.CourseUiModel

data class SavedCoursesUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val recentCourses: List<CourseUiModel> = emptyList(),
    val popularCourses: List<CourseUiModel> = emptyList(),
    val error: UiError? = null,
)
