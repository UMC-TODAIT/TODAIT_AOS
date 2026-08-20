package com.umc.todait.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.core.network.UiError
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Pink900
import com.umc.todait.ui.theme.TodaitTheme

/**
 * 네트워크 에러 알럿. 재시도 가능한 에러면 오른쪽 버튼이 [다시시도]([onRetry]), 아니면 [확인](닫기)이다.
 *
 * ⚠️ [onDismiss] 에는 기본값을 두지 않는다. 기본값이 있으면 호출부가 깜빡 잊었을 때
 * 왼쪽 [취소] 를 눌러도 아무 일도 일어나지 않는 알럿이 만들어지기 때문이다.
 * 알럿만 닫고 화면에 안내를 남기고 싶으면 [DismissibleErrorContent] 를 쓴다.
 */
@Composable
fun ErrorContent(
    error: UiError,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
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

/**
 * [취소] 로 닫으면 같은 자리에 인라인 안내가 남는 에러 표시.
 *
 * 목록 조회 실패처럼 알럿 뒤에 쓸 수 있는 화면(검색창·헤더·다른 탭)이 남아 있는 곳에서 쓴다.
 * 알럿을 닫아도 [InlineErrorMessage] 가 남아 있어 사용자가 언제든 다시 조회할 수 있다.
 *
 * 닫았는지 여부는 화면 로컬 상태로만 들고 있고, [error] 문구가 바뀌면(= 다른 에러) 알럿을 다시 띄운다.
 */
@Composable
fun DismissibleErrorContent(
    error: UiError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var alertVisible by remember(error) { mutableStateOf(true) }

    InlineErrorMessage(
        message = error.message,
        onRetry = onRetry,
        modifier = modifier,
    )

    if (alertVisible) {
        ErrorContent(
            error = error,
            onRetry = {
                alertVisible = false
                onRetry()
            },
            // [취소] 는 알럿만 닫는다. 뒤에 남는 인라인 안내로 다시 시도할 수 있다.
            onDismiss = { alertVisible = false },
        )
    }
}

/** 알럿을 닫은 뒤 화면에 남는 안내. 문구 아래 [다시 시도] 로 같은 조회를 한 번 더 부른다. */
@Composable
fun InlineErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Gray500,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "다시 시도",
            modifier = Modifier
                // 텍스트 버튼이라 터치 영역을 최소 44dp 로 넓혀 둔다.
                .heightIn(min = 44.dp)
                .clickable { onRetry() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Pink900,
        )
    }
}

@Preview(name = "에러 알럿 - 재시도", showBackground = true)
@Composable
private fun ErrorContentPreview() {
    TodaitTheme {
        ErrorContent(
            error = UiError(message = "일시적인 오류가 발생했어요.\n다시 시도해주세요."),
            onRetry = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "에러 인라인 안내", showBackground = true)
@Composable
private fun InlineErrorMessagePreview() {
    TodaitTheme {
        InlineErrorMessage(
            message = "노출 대상이 아닌 장소입니다.",
            onRetry = {},
        )
    }
}
