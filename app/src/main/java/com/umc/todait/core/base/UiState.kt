package com.umc.todait.core.base

/**
 * 목록/상세 조회처럼 로딩-성공-실패 3상태가 필요한 화면에서 사용하는 공통 상태.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Failure(val message: String) : UiState<Nothing>
}
