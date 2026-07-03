package com.umc.todait.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.todait.core.network.UiError

/**
 * 네트워크 에러 상태 공통 컴포넌트. (뼈대 — 실제 디자인 적용 예정)
 * UiState.Failure 일 때 노출하며, 재시도 가능한 에러면 [onRetry] 버튼을 보여준다.
 */
@Composable
fun ErrorContent(
    error: UiError,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = error.message)
        if (error.isRetryable && onRetry != null) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Text("다시 시도")
            }
        }
    }
}
