package com.umc.todait.feature.home.coursedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.home.data.repository.HomeRepository
import com.umc.todait.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 홈 "오늘의 추천 코스" 카드 탭 → 추천 코스 상세 화면(#55)의 상태를 관리한다.
 * 지니 담당 [com.umc.todait.feature.saved.compose.CourseDetailScreen](저장된 코스 상세)과는 별개 화면이다.
 */
@HiltViewModel
class RecommendedCourseDetailViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    private val courseId: Long = savedStateHandle[Screen.RecommendedCourseDetail.ARG_COURSE_ID] ?: 0L

    private val _uiState = MutableStateFlow(RecommendedCourseDetailUiState())
    val uiState: StateFlow<RecommendedCourseDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RecommendedCourseDetailEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            when (val result = homeRepository.getRecommendedCourseDetail(courseId)) {
                is ApiResult.Success -> _uiState.update { result.data.toUiState() }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, loadError = result.toUiError().message) }
            }
        }
    }

    /** 상단 "코스 저장" 버튼 → 저장 확인 알럿(CommonDialog)을 띄운다. */
    fun onSaveClick() {
        _uiState.update { it.copy(isSaveConfirmDialogVisible = true) }
    }

    fun onDismissSaveConfirm() {
        _uiState.update { it.copy(isSaveConfirmDialogVisible = false) }
    }

    /** 저장 확인 알럿 [확인] → 실제 저장 요청. 성공하면 저장 완료 알럿(CourseSaveDialog)을 띄운다. */
    fun onConfirmSave() {
        _uiState.update { it.copy(isSaveConfirmDialogVisible = false, isSaving = true, saveError = null) }
        viewModelScope.launch {
            when (val result = homeRepository.saveRecommendedCourse(courseId)) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, isSavedDialogVisible = true) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSaving = false, saveError = result.toUiError().message) }
            }
        }
    }

    /** 저장 완료 알럿 [저장된 코스로 이동하기]. */
    fun onMoveToSavedCourses() {
        _uiState.update { it.copy(isSavedDialogVisible = false) }
        viewModelScope.launch { _effect.send(RecommendedCourseDetailEffect.NavigateToSavedCourses) }
    }

    /** 저장 완료 알럿 [건너뛰기] — 알럿만 닫고 상세 화면에 머무른다. */
    fun onSkipSavedDialog() {
        _uiState.update { it.copy(isSavedDialogVisible = false) }
    }
}
