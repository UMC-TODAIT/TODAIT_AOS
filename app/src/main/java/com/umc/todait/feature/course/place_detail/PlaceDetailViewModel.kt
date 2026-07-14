package com.umc.todait.feature.course.place_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.course.data.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 장소 상세 화면(와이어프레임: 장소카드클릭_기본)의 상태를 관리한다.
 *
 * 진입 시 라우트 인자 placeId 로 장소 카드 정보(GET /api/places/{placeId})를 조회한다.
 */
@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Screen.PlaceDetail.ARG_PLACE_ID 와 동일한 키. 라우트에서 넘어온 대상 장소 id.
    private val placeId: Long = checkNotNull(savedStateHandle[ARG_PLACE_ID]) {
        "placeId 인자가 필요합니다."
    }

    private val _uiState = MutableStateFlow(PlaceDetailUiState())
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    init {
        loadPlaceDetail()
    }

    /** 장소 카드 정보 조회. 재시도에서도 재사용한다. */
    fun loadPlaceDetail() {
        _uiState.update { it.copy(detailState = PlaceDetailState.Loading) }
        viewModelScope.launch {
            val result = placeRepository.getPlaceDetail(placeId = placeId)
            _uiState.update { state ->
                val next = when (result) {
                    is ApiResult.Success -> PlaceDetailState.Success(result.data.toUiModel())
                    is ApiResult.Failure -> PlaceDetailState.Error(result.toUiError().message)
                }
                state.copy(detailState = next)
            }
        }
    }

    companion object {
        private const val ARG_PLACE_ID = "placeId"
    }
}
