package com.umc.todait.feature.auth.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.todait.R
import com.umc.todait.feature.auth.data.dto.TermAgreementDto
import com.umc.todait.ui.theme.DisabledButtonGray
import com.umc.todait.ui.theme.Gray300
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.Pink400
import com.umc.todait.ui.theme.Primary
import com.umc.todait.ui.theme.SignupBackground
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 약관 동의 화면(라우트 진입점). ViewModel의 상태/효과를 구독한다.
 * 필수 약관을 모두 동의하고 [다음]을 누르면 진입 플로우(이메일/카카오/구글)에 따라
 * 회원가입 폼 또는 소셜 닉네임 설정 화면으로 이동한다.
 * 필수 약관 항목의 화살표를 누르면 약관 상세 화면으로 이동한다.
 */
@Composable
fun TermsAgreementScreen(
    onBackClick: () -> Unit,
    onNext: (TermsFlow, List<TermAgreementDto>) -> Unit,
    onViewDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsAgreementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TermsAgreementEffect.NavigateNext -> onNext(effect.flow, effect.agreedTerms)
                is TermsAgreementEffect.NavigateToDetail -> onViewDetail(effect.termId)
            }
        }
    }

    TermsAgreementContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onToggleAll = viewModel::onToggleAll,
        onToggleTerm = viewModel::onToggleTerm,
        onViewDetail = viewModel::onViewDetail,
        onNextClick = viewModel::onNextClick,
        modifier = modifier,
    )
}

@Composable
private fun TermsAgreementContent(
    uiState: TermsAgreementUiState,
    onBackClick: () -> Unit,
    onToggleAll: () -> Unit,
    onToggleTerm: (Long) -> Unit,
    onViewDetail: (Long) -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SignupBackground),
    ) {
        TermsAgreementTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.terms_agreement_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = Gray900,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.terms_agreement_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
            )

            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TermCard {
                    AllAgreeRow(checked = uiState.isAllAgreed, onToggle = onToggleAll)
                }
                uiState.terms.forEach { term ->
                    TermCard {
                        TermRow(
                            term = term,
                            onToggle = { onToggleTerm(term.termId) },
                            onViewDetail = { onViewDetail(term.termId) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .height(56.dp)
                .clip(CircleShape)
                .background(if (uiState.isNextEnabled) Pink400 else DisabledButtonGray)
                .clickable(enabled = uiState.isNextEnabled, onClick = onNextClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.terms_agreement_next_button),
                style = MaterialTheme.typography.labelLarge,
                fontSize = 18.sp,
                color = if (uiState.isNextEnabled) White else Gray500,
            )
        }
    }
}

@Composable
private fun TermsAgreementTopBar(onBackClick: () -> Unit) {
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
                text = stringResource(R.string.terms_agreement_title),
                style = MaterialTheme.typography.titleMedium,
                color = Gray900,
            )
        }
        HorizontalDivider(color = Gray300)
    }
}

/** 약관 항목 하나를 감싸는 카드(테두리+둥근모서리). 전체동의/개별 약관 모두 같은 스타일을 쓴다. */
@Composable
private fun TermCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .border(1.dp, Gray300, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        content()
    }
}

@Composable
private fun AllAgreeRow(checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TermCheckCircle(checked = checked)
        Spacer(Modifier.size(12.dp))
        Text(
            text = stringResource(R.string.terms_agreement_all_agree),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Gray900,
        )
    }
}

@Composable
private fun TermRow(term: TermItemUiModel, onToggle: () -> Unit, onViewDetail: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TermCheckCircle(checked = term.isAgreed)
        Spacer(Modifier.size(12.dp))
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = term.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray900,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = stringResource(
                    if (term.isRequired) R.string.terms_agreement_tag_required else R.string.terms_agreement_tag_optional,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (term.isRequired) Primary else Gray500,
            )
        }
        if (term.hasDetail) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.terms_agreement_detail_content_description),
                tint = Gray500,
                // 상세 보기는 체크 토글과 별개 동작이라 화살표 영역에만 별도 클릭 리스너를 둔다.
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onViewDetail),
            )
        }
    }
}

@Composable
private fun TermCheckCircle(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (checked) Pink400 else White)
            .border(1.dp, if (checked) Pink400 else Gray300, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.terms_agreement_check_content_description),
                tint = White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

private val previewTerms = listOf(
    TermItemUiModel(1, "서비스 이용약관", isRequired = true, isAgreed = false, hasDetail = true),
    TermItemUiModel(2, "개인정보 수집 및 이용", isRequired = true, isAgreed = false, hasDetail = true),
    TermItemUiModel(3, "위치정보 이용 권한", isRequired = false, isAgreed = false),
    TermItemUiModel(4, "마케팅 푸시 알림", isRequired = false, isAgreed = false),
)

@Preview(showBackground = true, name = "기본")
@Composable
private fun TermsAgreementDefaultPreview() {
    TodaitTheme {
        TermsAgreementContent(
            uiState = TermsAgreementUiState(terms = previewTerms),
            onBackClick = {}, onToggleAll = {}, onToggleTerm = {}, onViewDetail = {}, onNextClick = {},
        )
    }
}

@Preview(showBackground = true, name = "전체 동의 · 다음 활성")
@Composable
private fun TermsAgreementAllAgreedPreview() {
    TodaitTheme {
        TermsAgreementContent(
            uiState = TermsAgreementUiState(terms = previewTerms.map { it.copy(isAgreed = true) }),
            onBackClick = {}, onToggleAll = {}, onToggleTerm = {}, onViewDetail = {}, onNextClick = {},
        )
    }
}
