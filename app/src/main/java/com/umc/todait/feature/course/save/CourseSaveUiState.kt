package com.umc.todait.feature.course.save

import com.umc.todait.feature.course.compose.CourseMood

/**
 * 코스 저장 화면(Figma "코스저장", node 534:13926)의 UI 상태.
 *
 * [com.umc.todait.feature.course.compose.SelectedPlacesScreen] 에서 ✓ 를 누르면 진입하는 마지막 단계.
 * 이름·메모·태그를 입력하고 헤더 ✓ 로 저장하면 저장된 코스로 이동한다.
 *
 * 경로 미리보기에 노출할 장소 목록은 이 상태가 아니라 코스 구성 플로우의 공유 ViewModel
 * ([com.umc.todait.feature.course.compose.CourseComposeViewModel])에서 받아 화면 인자로 전달한다.
 */
data class CourseSaveUiState(
    val name: String = "",
    val memo: String = "",
    // 선택된 분위기 태그(프리셋 6종 중). 화면에는 "# 라벨" 그라데이션 칩으로 노출한다. (Figma node 1243-6057)
    val selectedTags: Set<CourseMood> = emptySet(),
    // 태그 추가 바텀시트 노출 여부.
    val isTagSheetVisible: Boolean = false,
    // 바텀시트에서 편집 중인 임시 선택. ✓ 로 확정하면 [selectedTags] 에 반영, X 로 닫으면 버린다.
    val draftTags: Set<CourseMood> = emptySet(),
    // 이름 미입력 상태로 저장을 시도했을 때 뜨는 안내 알럿(CommonDialog) 노출 여부. (Figma node 918-2638)
    val isNameErrorDialogVisible: Boolean = false,
    // 이름 통과 후 저장을 시도했을 때 뜨는 확인 알럿(CommonDialog) 노출 여부. (Figma node 1204-4231)
    val isSaveConfirmDialogVisible: Boolean = false,
    // 저장 완료 다이얼로그(ui/component 의 CourseSaveDialog) 노출 여부.
    val isSavedDialogVisible: Boolean = false,
) {
    /** 코스 이름은 필수. 이름이 있어야 저장(체크)이 가능하다. */
    val canSave: Boolean get() = name.isNotBlank()

    /** 화면 표시용: 선택된 태그를 프리셋 노출 순서([TAG_PRESETS])대로 정렬한 목록. */
    val orderedTags: List<CourseMood> get() = TAG_PRESETS.filter { it in selectedTags }

    companion object {
        const val MAX_NAME_LENGTH = 30
        const val MAX_MEMO_LENGTH = 200

        // 태그 추가 시트의 프리셋 노출 순서. (Figma node 1243-6988: 로맨틱·힙한·활발한·조용한·모던한·차분한)
        val TAG_PRESETS = listOf(
            CourseMood.ROMANTIC,
            CourseMood.HIP,
            CourseMood.ACTIVE,
            CourseMood.QUIET,
            CourseMood.MODERN,
            CourseMood.CALM,
        )
    }
}

/** 코스 저장 화면에서 화면 전환이 필요한 일회성 이벤트. */
sealed interface CourseSaveEffect {
    /** 저장 완료 다이얼로그 [저장된 코스로 이동하기] → 저장된 코스 목록으로. */
    data object NavigateToSavedCourses : CourseSaveEffect

    /** 저장 완료 다이얼로그 [건너뛰기] → 홈으로. */
    data object NavigateToHome : CourseSaveEffect
}
