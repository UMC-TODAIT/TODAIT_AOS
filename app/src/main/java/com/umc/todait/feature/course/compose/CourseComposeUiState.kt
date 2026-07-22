package com.umc.todait.feature.course.compose

import com.umc.todait.feature.course.base_place.PlaceUiModel
import com.umc.todait.feature.course.data.dto.PlaceCategoryResponseDto

/**
 * 코스 구성하기 플로우(#26, 와이어프레임 "코스구성하기_수정버전")의 공유 UI 상태.
 *
 * **두 화면이 이 상태를 공유한다**(NavHost 의 course/compose 중첩 그래프에 스코프된 ViewModel):
 * 1) [CourseComposeScreen] (장소카드 선택): [상단 지도] + [카테고리 탭] + [추천 카드('+' 담기)]. 헤더 ✓ → 2)로 이동.
 * 2) [com.umc.todait.feature.course.compose.SelectedPlacesScreen] (선택한 장소): [지도] + "선택한 장소(N)" 드래그 순서 조정. 헤더 ✓ → 코스 저장.
 *
 * - 기준 장소([basePlace], #11에서 확정)를 출발점으로, 카테고리별 추천 장소를 [recommendState] 로 노출.
 * - 카드 '+' 로 코스에 담고([selectedPlaces], 담은 순서 = 코스 순서), 2) 화면에서 드래그로 순서를 수정한다.
 * - 이미 담긴 장소를 다시 담으면 [alert] 로 중복 안내.
 *
 * ⚠️ 지도는 카카오맵 v2([CourseMap])로 연동돼 있다(런타임 키/렌더 확인은 별도).
 * 드래그 순서 변경 제스처/카테고리별 추천 필터/임시 코스 세션 연동은 TODO 로 남겨 후속 확장한다.
 */
data class CourseComposeUiState(
    // 기준 장소(코스 출발점). 임시 코스 세션 API 연동 전이라 현재는 null 가능.
    val basePlace: PlaceUiModel? = null,
    // 임시 코스(course-draft) 핸들. 진입 시 POST /api/course-drafts 로 발급. null 이면 미발급/실패.
    val courseDraftId: Long? = null,
    // 카테고리 탭(장소 대분류). GET /api/place-categories 로 로드. 비어 있으면 탭 미표시.
    val categories: List<PlaceCategoryUiModel> = emptyList(),
    // 현재 선택된 카테고리 id. null 이면 미선택.
    val selectedCategoryId: Long? = null,
    val recommendState: RecommendListState = RecommendListState.Loading,
    // 코스에 담은 장소들. index 0 = 기준 장소 다음 첫 장소. 순서 = 코스 동선.
    val selectedPlaces: List<PlaceUiModel> = emptyList(),
    // 노출 중인 시스템 알럿(중복 선택 등). null 이면 닫힘.
    val alert: CourseComposeAlert? = null,
) {
    /** 담은 장소가 하나라도 있어야 확정(다음 단계) 가능. */
    val canConfirm: Boolean get() = selectedPlaces.isNotEmpty()
}

/**
 * 카테고리 탭(장소 대분류: 카페/식당/액티비티/술). GET /api/place-categories 매핑.
 * 하드코딩 enum 을 대체하며, 라벨/개수는 서버 응답을 따른다.
 */
data class PlaceCategoryUiModel(
    val id: Long,
    val name: String,
)

/** 장소 대분류 DTO → 화면 탭 모델. */
fun PlaceCategoryResponseDto.toUiModel(): PlaceCategoryUiModel = PlaceCategoryUiModel(
    id = placeCategoryId,
    name = name,
)

/**
 * 추천 장소 카드의 분위기(mood). Figma 카드 컴포넌트가 분위기별로 다른 그라데이션/장식을 쓴다.
 *
 * 종류는 "분위기 태그 조회"(GET /api/mood-tags) 명세의 6종(mood_tag)과 일치시킨다.
 * [code] = mood_tag.code, [label] = mood_tag.name.
 * 그라데이션 색은 Figma "취향설정" 화면 기준으로 6종 전부 확정(색상 토큰은 Color.kt 참고).
 * 우측 하단 아이콘도 6종 각각 전용 에셋(ic_mood_*)으로 매칭한다.
 *
 * [PlaceUiModel.moodTags] 의 분위기 태그(code 또는 name)로 결정한다([fromTags]). 추천 API 응답에는
 * 분위기 태그가 없어(matchedMoodCount 만 존재) 태그가 비어 있을 때는 화면에서 fallback 을 부여한다.
 */
enum class CourseMood(val code: String, val label: String) {
    HIP("HIP", "힙한"),
    QUIET("QUIET", "조용한"),
    ACTIVE("ACTIVE", "활발한"),
    ROMANTIC("ROMANTIC", "로맨틱"),
    MODERN("MODERN", "모던한"),
    CALM("CALM", "차분한"),
    ;

    companion object {
        /** 분위기 태그 목록(code 또는 name)에서 첫 번째로 매칭되는 분위기를 찾는다. 없으면 null. */
        fun fromTags(tags: List<String>): CourseMood? =
            tags.firstNotNullOfOrNull { raw ->
                val t = raw.trim()
                entries.firstOrNull { it.code.equals(t, ignoreCase = true) || it.label == t }
            }
    }
}

/** 추천 장소 목록 영역의 상태. */
sealed interface RecommendListState {
    data object Loading : RecommendListState
    data class Success(val places: List<PlaceUiModel>) : RecommendListState
    data class Empty(val message: String) : RecommendListState
    data class Error(val message: String) : RecommendListState
}

/** 코스 구성하기 화면의 시스템 알럿 종류. */
sealed interface CourseComposeAlert {
    /** 이미 담은 장소를 다시 담으려 할 때(중복선택 알럿). */
    data object Duplicate : CourseComposeAlert
}
