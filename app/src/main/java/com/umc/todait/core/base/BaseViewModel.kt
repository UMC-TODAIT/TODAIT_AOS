package com.umc.todait.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.UiError
import com.umc.todait.core.network.toUiError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * 모든 ViewModel의 베이스.
 * - 공통 에러를 errorFlow로 방출 → 화면에서 스낵바/다이얼로그로 표시
 * - launchApi로 API 호출 + 에러 처리 보일러플레이트 제거
 *
 * 참고: 단일 Activity + Compose 구조이므로 BaseActivity/BaseFragment 대신
 * BaseViewModel + 공통 컴포저블(ui/component)로 공통 처리를 담당한다.
 */
abstract class BaseViewModel : ViewModel() {

    private val _errorFlow = MutableSharedFlow<UiError>()
    val errorFlow: SharedFlow<UiError> = _errorFlow.asSharedFlow()

    /**
     * API 호출 공통 래퍼.
     * onError를 지정하지 않으면 errorFlow로 공통 에러 처리(스낵바 등)에 위임한다.
     */
    protected fun <T> launchApi(
        apiCall: suspend () -> ApiResult<T>,
        onError: ((UiError) -> Unit)? = null,
        onSuccess: (T) -> Unit,
    ) {
        viewModelScope.launch {
            when (val result = apiCall()) {
                is ApiResult.Success -> onSuccess(result.data)
                is ApiResult.Failure -> {
                    val uiError = result.toUiError()
                    onError?.invoke(uiError) ?: _errorFlow.emit(uiError)
                }
            }
        }
    }

    protected suspend fun emitError(error: UiError) = _errorFlow.emit(error)
}
