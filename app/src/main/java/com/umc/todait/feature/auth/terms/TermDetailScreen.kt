package com.umc.todait.feature.auth.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.todait.R
import com.umc.todait.ui.theme.Gray300
import com.umc.todait.ui.theme.Gray700
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.SignupBackground
import com.umc.todait.ui.theme.TodaitTheme

/**
 * 약관 상세 화면. 약관 동의 화면에서 필수 약관 항목(화살표)을 탭하면 진입한다.
 *
 * GET /api/terms/{termId}는 담당자 코멘트로 "노션으로 이동, API 필요없음" 처리됐었는데,
 * 실제 디자인 시안엔 앱 내 상세 화면이 있어 시안 기준으로 구현한다. 내용은 아직 확정 카피가
 * 없어 더미 텍스트(termDetails)로 채우고, 실제 약관 문구가 나오면 교체한다.
 */
@Composable
fun TermDetailScreen(
    termId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detail = termDetails[termId] ?: termDetails.getValue(DEFAULT_TERM_ID)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SignupBackground),
    ) {
        TermDetailTopBar(title = detail.title, onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = detail.article,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = detail.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700,
            )
        }
    }
}

@Composable
private fun TermDetailTopBar(title: String, onBackClick: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_button),
                    contentDescription = stringResource(R.string.terms_agreement_back_content_description),
                    tint = Color.Unspecified,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Gray900,
            )
        }
        HorizontalDivider(color = Gray300)
    }
}

private data class TermDetailInfo(val title: String, val article: String, val description: String)

private const val DEFAULT_TERM_ID = 1L

// TODO: 실제 약관 문구/개인정보 처리방침 확정되면 교체.
private val termDetails = mapOf(
    1L to TermDetailInfo(title = "서비스 이용 약관", article = "약관 제 1조", description = "설명"),
    2L to TermDetailInfo(title = "개인정보 수집 및 이용", article = "개인정보 이용 안내", description = "설명"),
)

@Preview(showBackground = true, name = "서비스 이용약관")
@Composable
private fun TermDetailServicePreview() {
    TodaitTheme {
        TermDetailScreen(termId = 1L, onBackClick = {})
    }
}

@Preview(showBackground = true, name = "개인정보 수집 및 이용")
@Composable
private fun TermDetailPrivacyPreview() {
    TodaitTheme {
        TermDetailScreen(termId = 2L, onBackClick = {})
    }
}
