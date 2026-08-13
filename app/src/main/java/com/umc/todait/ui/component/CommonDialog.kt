package com.umc.todait.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.umc.todait.ui.theme.DividerLine
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Pink900
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 취소/확인 두 갈래 시스템 알럿 (Figma `컴포넌트_System` 의 시스템알럿 1~3).
 *
 * 시안은 문구 줄 수에 따라 112 / 120 / 142 세 벌로 나뉘는데, 공통 규칙은
 * "위아래 17dp 여백(한 줄일 땐 최소 64dp) + 48dp 버튼 행"이라 높이를 고정하지 않고 내용으로 잡는다.
 * 폭도 304dp 고정 대신 좌우 여백과 최대 폭만 잡아 화면 폭에 대응한다.
 *
 * [confirmText] 가 강조(Pink-900)되는 오른쪽 버튼, [cancelText] 가 왼쪽(Gray-200)이다.
 * 시안에서 강조되는 쪽이 항상 "확인"은 아니므로(예: 메모 이탈 알럿은 "계속 수정하기"가 강조)
 * 호출부에서 어느 쪽을 강조할지 정해 넘긴다.
 */
@Composable
fun CommonDialog(
    title: String,
    confirmText: String = "확인",
    cancelText: String = "취소",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    // 왼쪽 버튼이 되돌릴 수 없는 동작일 때(예: "나가기") 바깥 탭/뒤로가기로 그게 실행되면 안 되므로 분리한다.
    onDismissRequest: () -> Unit = onDismiss,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        // 시스템 기본 폭 대신 화면 폭 기준으로 직접 잡는다.
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 44.dp)
                .fillMaxWidth()
                .widthIn(max = 360.dp),
            shape = RoundedCornerShape(12.dp),
            color = White
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 한 줄짜리 문구(시스템알럿, 112dp)에서도 시안 높이가 나오도록 최소 높이를 준다.
                        .heightIn(min = 64.dp)
                        .padding(horizontal = 8.dp, vertical = 17.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(
                    color = DividerLine
                )

                // 세로 구분선이 버튼 높이를 따라가도록 행 높이를 내용 기준으로 잡는다.
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .fillMaxHeight()
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cancelText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Gray200
                        )
                    }

                    VerticalDivider(
                        color = DividerLine
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .fillMaxHeight()
                            .clickable { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = confirmText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Pink900
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "시스템알럿 - 한 줄", showBackground = true)
@Composable
private fun CommonDialogSingleLinePreview() {
    TodaitTheme {
        CommonDialog(
            title = "기준 장소를 ‘뀌노이’로 시작할까요?",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "시스템알럿 - 세 줄", showBackground = true)
@Composable
private fun CommonDialogMultiLinePreview() {
    TodaitTheme {
        CommonDialog(
            title = "분위기를 변경하면\n저장했던 장소 정보가 초기화됩니다.\n변경하시겠습니까?",
            confirmText = "변경하기",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
