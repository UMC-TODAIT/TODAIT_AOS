package com.umc.todait.feature.course.base_place

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.todait.R
import com.umc.todait.ui.theme.Error
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Primary
import com.umc.todait.ui.theme.TodaitTheme

/**
 * 기준 장소 확인 모달(와이어프레임 1.2).
 *
 * "기준 장소를 '{장소명}'으로 시작할까요?" 확인 문구와 설명을 노출하고,
 * [확인]/[취소] 버튼을 제공한다. 지원 지역 외 등 예외 발생 시 [errorMessage] 를 함께 표시한다.
 */
@Composable
fun BasePlaceConfirmDialog(
    placeName: String,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.base_place_confirm_title, placeName),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.base_place_confirm_desc, placeName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500,
                )
                if (errorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.base_place_confirm_positive),
                    color = Primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.base_place_confirm_negative),
                    color = Gray500,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

@Preview
@Composable
private fun BasePlaceConfirmDialogPreview() {
    TodaitTheme {
        BasePlaceConfirmDialog(
            placeName = "연남동 감성카페",
            errorMessage = null,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "지원 지역 외 예외")
@Composable
private fun BasePlaceConfirmDialogErrorPreview() {
    TodaitTheme {
        BasePlaceConfirmDialog(
            placeName = "강남 어딘가",
            errorMessage = "현재는 홍대, 연남, 성수 지역만 코스 생성을 지원해요.",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
