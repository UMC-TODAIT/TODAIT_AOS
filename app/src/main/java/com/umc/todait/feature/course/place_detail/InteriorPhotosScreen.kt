package com.umc.todait.feature.course.place_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.todait.R
import com.umc.todait.core.network.UiError
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray800

/**
 * 내부 사진 전체보기 화면(와이어프레임: 장소카드클릭_내부사진전체보기).
 *
 * 상단은 장소 상세와 동일한 사진 캐러셀([PhotoCarousel])을 재사용하고,
 * 그 아래 "← 내부 사진" 헤더 + 2열 메이슨리(엇갈린) 그리드로 내부 사진을 나열한다.
 *
 * 데이터는 장소 상세와 같은 [PlaceDetailViewModel] 을 재사용한다.
 * (사진 화면 라우트도 placeId 인자를 갖고 있어 동일하게 조회된다.)
 */
@Composable
fun InteriorPhotosScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        when (val detail = uiState.detailState) {
            is PlaceDetailState.Loading -> LoadingIndicator()

            is PlaceDetailState.Error -> {
                ErrorContent(
                    error = UiError(message = detail.message),
                    onRetry = viewModel::loadPlaceDetail,
                )
                OverlayBackButton(onBack = onBack)
            }

            is PlaceDetailState.Success -> InteriorPhotosBody(
                imageUrls = detail.place.imageUrls,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun InteriorPhotosBody(
    imageUrls: List<String>,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // 상단 히어로 캐러셀(장소 상세와 동일).
        PhotoCarousel(imageUrls = imageUrls, onBack = onBack)

        // "← 내부 사진" 서브 헤더.
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Gray800,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onBack),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.place_detail_section_photos),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray800,
            )
        }

        Spacer(Modifier.height(16.dp))
        PhotoMasonryGrid(imageUrls = imageUrls)
        Spacer(Modifier.height(24.dp))
    }
}

/**
 * 2열 메이슨리(엇갈린 높이) 그리드.
 *
 * 이미지를 인덱스 짝/홀로 좌·우 열에 나눠 담고, 각 이미지는 원본 비율대로 높이를 잡아
 * 시안의 엇갈린 배치를 재현한다. 열 간격·이미지 간격은 16dp.
 */
@Composable
private fun PhotoMasonryGrid(imageUrls: List<String>) {
    val leftColumn = imageUrls.filterIndexed { index, _ -> index % 2 == 0 }
    val rightColumn = imageUrls.filterIndexed { index, _ -> index % 2 == 1 }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MasonryColumn(imageUrls = leftColumn, modifier = Modifier.weight(1f))
        MasonryColumn(imageUrls = rightColumn, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MasonryColumn(imageUrls: List<String>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        imageUrls.forEach { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
