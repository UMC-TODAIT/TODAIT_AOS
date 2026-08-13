package com.umc.todait.feature.course.mood

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.umc.todait.R
import com.umc.todait.feature.course.data.dto.MoodTagDto
import com.umc.todait.ui.theme.CourseActiveGradientEnd
import com.umc.todait.ui.theme.CourseActiveGradientStart
import com.umc.todait.ui.theme.CourseCalmGradientEnd
import com.umc.todait.ui.theme.CourseCalmGradientStart
import com.umc.todait.ui.theme.CourseHipGradientEnd
import com.umc.todait.ui.theme.CourseHipGradientStart
import com.umc.todait.ui.theme.CourseModernGradientEnd
import com.umc.todait.ui.theme.CourseModernGradientStart
import com.umc.todait.ui.theme.CourseQuietGradientEnd
import com.umc.todait.ui.theme.CourseQuietGradientStart
import com.umc.todait.ui.theme.CourseRomanticGradientEnd
import com.umc.todait.ui.theme.CourseRomanticGradientStart
import com.umc.todait.ui.theme.MoodActiveSelectedGradientEnd
import com.umc.todait.ui.theme.MoodActiveSelectedGradientStart
import com.umc.todait.ui.theme.MoodCalmSelectedGradientEnd
import com.umc.todait.ui.theme.MoodCalmSelectedGradientStart
import com.umc.todait.ui.theme.MoodHipSelectedGradientEnd
import com.umc.todait.ui.theme.MoodHipSelectedGradientStart
import com.umc.todait.ui.theme.MoodModernSelectedGradientEnd
import com.umc.todait.ui.theme.MoodModernSelectedGradientStart
import com.umc.todait.ui.theme.MoodQuietSelectedGradientEnd
import com.umc.todait.ui.theme.MoodQuietSelectedGradientStart
import com.umc.todait.ui.theme.MoodRomanticSelectedGradientEnd
import com.umc.todait.ui.theme.MoodRomanticSelectedGradientStart

/**
 * 분위기 선택 화면의 UI 상태.
 *
 * 선택지는 `GET /api/mood-tags` 로 받아오므로 진입 시 [listState] 가 Loading 으로 시작한다.
 * 저장 요청에는 [MoodOptionUiModel.moodTagId] 를 그대로 실어 보낸다.
 */
data class MoodSelectUiState(
    val listState: MoodListState = MoodListState.Loading,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
) {
    private val moods: List<MoodOptionUiModel>
        get() = (listState as? MoodListState.Success)?.moods.orEmpty()

    val selectedCount: Int get() = moods.count { it.isSelected }

    /** 선택한 분위기 태그 id 목록(저장 API 요청값). */
    val selectedMoodTagIds: List<Long> get() = moods.filter { it.isSelected }.map { it.moodTagId }

    /** 명세: 최소 2개 이상 최대 6개 이하일 때만 저장할 수 있다. */
    val isConfirmEnabled: Boolean
        get() = selectedCount in MIN_SELECTION..MAX_SELECTION && !isSubmitting

    companion object {
        const val MIN_SELECTION = 2
        const val MAX_SELECTION = 6
    }
}

/** 분위기 목록 영역의 상태. */
sealed interface MoodListState {
    data object Loading : MoodListState
    data class Success(val moods: List<MoodOptionUiModel>) : MoodListState
    data class Error(val message: String) : MoodListState
}

/**
 * 분위기 카드 한 장.
 *
 * [moodTagId]·[title] 은 서버(`GET /api/mood-tags`)에서 오고,
 * 카드 색상·문양·해시태그는 서버에 없는 값이라 [code] 로 앱 리소스를 찾아 붙인다.
 */
data class MoodOptionUiModel(
    val moodTagId: Long,
    val code: String,
    val title: String,
    val hashtags: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val selectedGradientStart: Color,
    val selectedGradientEnd: Color,
    @DrawableRes val decorationRes: Int,
    val isSelected: Boolean = false,
)

/**
 * 분위기 태그 DTO → 카드 모델. 표시 순서는 서버 sortOrder 를 따르므로 여기서 정렬하지 않는다.
 * [isSelected] 는 화면에서 토글하는 값이라 항상 false 로 시작한다.
 */
fun MoodTagDto.toUiModel(): MoodOptionUiModel {
    val visual = MOOD_VISUALS[code] ?: FALLBACK_VISUAL
    return MoodOptionUiModel(
        moodTagId = moodTagId,
        code = code,
        title = name,
        hashtags = visual.hashtags,
        gradientStart = visual.gradientStart,
        gradientEnd = visual.gradientEnd,
        selectedGradientStart = visual.selectedGradientStart,
        selectedGradientEnd = visual.selectedGradientEnd,
        decorationRes = visual.decorationRes,
    )
}

/** 카드 1장의 시각 요소(Figma "취향설정"). 서버 응답에 없는 값이라 code 로 매칭한다. */
private data class MoodVisual(
    val hashtags: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val selectedGradientStart: Color,
    val selectedGradientEnd: Color,
    @DrawableRes val decorationRes: Int,
)

private val MOOD_VISUALS: Map<String, MoodVisual> = mapOf(
    "HIP" to MoodVisual(
        hashtags = "#트렌디 #감각적",
        gradientStart = CourseHipGradientStart, gradientEnd = CourseHipGradientEnd,
        selectedGradientStart = MoodHipSelectedGradientStart, selectedGradientEnd = MoodHipSelectedGradientEnd,
        decorationRes = R.drawable.ic_mood_hip,
    ),
    "QUIET" to MoodVisual(
        hashtags = "#차분 #잔잔",
        gradientStart = CourseQuietGradientStart, gradientEnd = CourseQuietGradientEnd,
        selectedGradientStart = MoodQuietSelectedGradientStart, selectedGradientEnd = MoodQuietSelectedGradientEnd,
        decorationRes = R.drawable.ic_mood_quiet,
    ),
    "ACTIVE" to MoodVisual(
        hashtags = "#에너지 #액티비티",
        gradientStart = CourseActiveGradientStart, gradientEnd = CourseActiveGradientEnd,
        selectedGradientStart = MoodActiveSelectedGradientStart, selectedGradientEnd = MoodActiveSelectedGradientEnd,
        decorationRes = R.drawable.ic_mood_active,
    ),
    "ROMANTIC" to MoodVisual(
        hashtags = "#분위기 #낭만",
        gradientStart = CourseRomanticGradientStart, gradientEnd = CourseRomanticGradientEnd,
        selectedGradientStart = MoodRomanticSelectedGradientStart,
        selectedGradientEnd = MoodRomanticSelectedGradientEnd,
        decorationRes = R.drawable.ic_mood_romantic,
    ),
    "MODERN" to MoodVisual(
        hashtags = "#도시적 #세련된",
        gradientStart = CourseModernGradientStart, gradientEnd = CourseModernGradientEnd,
        selectedGradientStart = MoodModernSelectedGradientStart, selectedGradientEnd = MoodModernSelectedGradientEnd,
        decorationRes = R.drawable.ic_mood_modern,
    ),
    "CALM" to MoodVisual(
        hashtags = "#내추럴 #편한",
        gradientStart = CourseCalmGradientStart, gradientEnd = CourseCalmGradientEnd,
        selectedGradientStart = MoodCalmSelectedGradientStart, selectedGradientEnd = MoodCalmSelectedGradientEnd,
        decorationRes = R.drawable.ic_mood_calm,
    ),
)

// 서버에 분위기 태그가 추가돼 앱에 없는 code 가 내려와도 카드가 비지 않도록 하는 기본값.
private val FALLBACK_VISUAL = MoodVisual(
    hashtags = "",
    gradientStart = CourseCalmGradientStart,
    gradientEnd = CourseCalmGradientEnd,
    selectedGradientStart = MoodCalmSelectedGradientStart,
    selectedGradientEnd = MoodCalmSelectedGradientEnd,
    decorationRes = R.drawable.ic_mood_calm,
)

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface MoodSelectEffect {
    /** 저장 성공 → 음식 선택 화면으로 이동. 발급/재사용된 임시 코스 핸들을 들고 간다. */
    data class NavigateToFood(val courseDraftId: Long) : MoodSelectEffect
}
