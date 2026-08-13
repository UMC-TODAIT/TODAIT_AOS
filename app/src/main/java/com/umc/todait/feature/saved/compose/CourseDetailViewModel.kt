package com.umc.todait.feature.saved.compose

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
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
                            error = null,
                            isLoading = false,

                            title = data.title,
                            date = data.savedDate,

                            moodTag = data.representativeMoodTag?.name,
                            moodTagCode = data.representativeMoodTag?.code,

                            placeTag = data.representativePlaceCategory?.name,
                            placeTagCode = data.representativePlaceCategory?.code,

                            memo = data.memo ?: "",

                            places = data.places.map { place -> PlaceUiModel(
                                placeId = place.placeId,
                                name = place.name,
                                address = place.address,
                                imageUrl = place.representativeImageUrl?.takeIf { it.isNotBlank() }
                            )
                            }
                        )
                    }
                }

                is ApiResult.Failure -> {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.toUiError()
                        )
                    }
                }
            }
        }
    }

    fun updateCourseMemo(
        courseId: Long,
        memo: String?
    ) {
        viewModelScope.launch {
            when (val result = savedRepository.updateCourseMemo(courseId, memo)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            memo = result.data.memo ?: "",
                            error = null
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(error = result.toUiError())
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }
}
