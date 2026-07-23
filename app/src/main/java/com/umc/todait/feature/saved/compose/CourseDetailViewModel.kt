package com.umc.todait.feature.saved.compose

import androidx.lifecycle.viewModelScope
import com.umc.todait.R
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.network.ApiResult
import com.umc.todait.feature.saved.PlaceUiModel
import com.umc.todait.feature.saved.data.repository.SavedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val savedRepository: SavedRepository
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState = _uiState.asStateFlow()
    fun getCourseDetail(courseId: Long) {
        _uiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            when (
                val result = savedRepository.getCourseDetail(courseId)
            ) {
                is ApiResult.Success -> {
                    val data = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,

                            title = data.title,
                            date = data.savedDate,

                            moodTag = data.representativeMoodTag?.name,
                            moodTagCode = data.representativeMoodTag?.code,

                            foodTag = data.representativeFoodCategory?.name,
                            foodTagCode = data.representativeFoodCategory?.code,

                            memo = data.memo ?: "",

                            places = data.places.map { place -> PlaceUiModel(
                                    isStartPlace = place.visitOrder == 1,
                                    name = place.name,
                                    address = place.address,
                                    backgroundImage = getMoodBackground(
                                        data.representativeMoodTag?.code
                                    ),
                                    memo = place.memo ?: ""
                                )
                            }
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
}
private fun getMoodBackground(
    code: String?
): Int {
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