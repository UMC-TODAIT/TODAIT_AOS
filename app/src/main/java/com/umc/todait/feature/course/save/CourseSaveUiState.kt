package com.umc.todait.feature.course.save

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
    // 저장할 태그들(입력 순서 유지). 화면에는 "# 태그" 칩으로 노출한다.
    val tags: List<String> = emptyList(),
    // 태그 입력 중인 값. null 이면 입력창이 닫힌 상태('+' 버튼만 노출).
    val pendingTag: String? = null,
    // 이름 미입력 상태로 저장을 시도했을 때의 안내 문구. null 이면 표시 안 함.
    val nameError: String? = null,
    // 저장 완료 다이얼로그(ui/component 의 CourseSaveDialog) 노출 여부.
    val isSavedDialogVisible: Boolean = false,
) {
    /** 코스 이름은 필수. 이름이 있어야 저장(체크)이 가능하다. */
    val canSave: Boolean get() = name.isNotBlank()

    /** 태그를 더 추가할 수 있는지(최대 [MAX_TAG_COUNT] 개). */
    val canAddTag: Boolean get() = tags.size < MAX_TAG_COUNT

    companion object {
        const val MAX_NAME_LENGTH = 30
        const val MAX_MEMO_LENGTH = 200
        const val MAX_TAG_LENGTH = 10
        const val MAX_TAG_COUNT = 5
    }
}

/** 코스 저장 화면에서 화면 전환이 필요한 일회성 이벤트. */
sealed interface CourseSaveEffect {
    /** 저장 완료 다이얼로그 [저장된 코스로 이동하기] → 저장된 코스 목록으로. */
    data object NavigateToSavedCourses : CourseSaveEffect

    /** 저장 완료 다이얼로그 [건너뛰기] → 홈으로. */
    data object NavigateToHome : CourseSaveEffect
}
