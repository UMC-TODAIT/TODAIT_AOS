package com.umc.todait.feature.saved.compose

import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.network.ApiResult
import com.umc.todait.feature.saved.CourseUiModel
import com.umc.todait.feature.saved.data.dto.SavedCourseDto
import com.umc.todait.feature.saved.data.repository.SavedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.umc.todait.R
import com.umc.todait.feature.mypage.data.repository.MyPageRepository

@HiltViewModel
class SavedCoursesViewModel @Inject constructor(
    private val savedRepository: SavedRepository,
    private val myPageRepository: MyPageRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SavedCoursesUiState())
    val uiState: StateFlow<SavedCoursesUiState> = _uiState.asStateFlow()

    init {
        getSavedCourses()
        getNickname()
    }

    private fun getSavedCourses() {
        _uiState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch {
            when (val result = savedRepository.getSavedCourses()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recentCourses = result.data.recentCourses.map { it.toUiModel() },
                            frequentlyViewedCourses = result.data.frequentlyViewedCourses.map { it.toUiModel() },
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun getNickname() {
        _uiState.update {
            it.copy(
                nickname = "투데잇"
            )

    /*
    실제 API 연결 시
    viewModelScope.launch {
        when(val result = myPageRepository.getMyPage()){

            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(
                        nickname = result.data.nickname
                    )
                }
            }

            is ApiResult.Failure -> { }
        }
    }

     */
        }
    }
}
private fun SavedCourseDto.toUiModel(): CourseUiModel {
    return CourseUiModel(
        id = courseId,

        backgroundImage = getBackgroundImage(representativeMoodTag?.code),
        topImage = getTopImage(representativeMoodTag?.code),

        title = title,
        date = savedDate,

        moodTag = representativeMoodTag?.name,
        foodTag = representativeFoodCategory?.name,

        places = previewPlaces.map { it.name }
    )
}
private fun getBackgroundImage(code: String?): Int {
    return when (code) {
        "ROMANTIC" -> R.drawable.bg_saved_courses_romantic
        "QUIET" -> R.drawable.bg_saved_courses_quiet
        "MODERN" -> R.drawable.bg_saved_courses_modern
        "HIP" -> R.drawable.bg_saved_courses_hip
        "ACTIVE" -> R.drawable.bg_saved_courses_active
        "CALM" -> R.drawable.bg_saved_courses_calm
        else -> R.drawable.bg_saved_courses_romantic
    }
}

private fun getTopImage(code: String?): Int {
    return when (code) {
        "ROMANTIC" -> R.drawable.ic_mood_romantic
        "QUIET" -> R.drawable.ic_mood_quiet
        "MODERN" -> R.drawable.ic_mood_modern
        "HIP" -> R.drawable.ic_mood_hip
        "ACTIVE" -> R.drawable.ic_mood_active
        "CALM" -> R.drawable.ic_mood_calm
        else -> R.drawable.ic_mood_romantic
    }
}