package com.umc.todait.ui.component

import androidx.compose.runtime.Composable
import com.umc.todait.core.network.UiError
import androidx.compose.ui.tooling.preview.Preview

/**
 * 네트워크 에러 상태 공통 컴포넌트. (뼈대 — 실제 디자인 적용 예정)
 * UiState.Failure 일 때 노출하며, 재시도 가능한 에러면 [onRetry] 버튼을 보여준다.
 */

@Preview(showBackground = true)
@Composable
fun ErrorContentPreview() {
    ErrorContent(
        error = UiError(
            message = "일시적인 오류가 발생했습니다.",
            isRetryable = true
        ),
        onRetry = {},
        onDismiss = {}
    )
}
@Composable
fun ErrorContent(
    error: UiError,
    onRetry: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    if (error.isRetryable) {
        CommonDialog(
            title = error.message,
            cancelText = "취소",
            confirmText = "다시시도",
            onDismiss = onDismiss,
            onConfirm = onRetry
        )
    } else {
        CommonDialog(
            title = error.message,
            cancelText = "취소",
            confirmText = "확인",
            onDismiss = onDismiss,
            onConfirm = onDismiss
        )
    }
}
