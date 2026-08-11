package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "코스 저장 요청"(POST /api/course-drafts/{courseDraftId}/courses)의 요청 바디.
 *
 * 임시 코스 저장 화면에서 사용자가 최종 입력/수정하는 값만 보낸다.
 * 음식 카테고리·기준 장소·선택 장소·방문 순서는 이미 임시 코스에 저장돼 있어 다시 전달하지 않는다.
 *
 * ⚠️ 코스 이름의 표준 필드명은 [title] 이다(기존 문서의 courseName 은 사용하지 않는다).
 * [moodTagIds] 는 코스 생성 초기 취향 선택을 복사하는 것이 아니라 **저장 화면에서 확정한 태그 전체**다.
 */
data class CourseSaveRequestDto(
    // 앞뒤 공백을 제거한 값. 빈 문자열이면 COURSE_TITLE400.
    @SerializedName("title") val title: String,
    // 선택값. 미입력이면 null 로 보낸다.
    @SerializedName("memo") val memo: String?,
    // 2개 이상 6개 이하, 중복 불가. 배열 순서에는 의미가 없다.
    @SerializedName("moodTagIds") val moodTagIds: List<Long>,
)

/**
 * 코스 저장 result. 성공하면 임시 코스 상태가 SAVING → COMPLETED 로 전이하며,
 * 같은 courseDraftId 로 다시 저장을 요청하면 COURSE_DRAFT409 가 내려온다.
 *
 * ⚠️ 배포 서버(2026-08-11)는 명세와 달리 추천 코스 저장용 응답(savedCourseId/sourceType/visibility)을
 * 그대로 돌려주고 있어, 코스 id 는 [courseId] 와 savedCourseId 를 함께 받는다.
 * 명세에만 있는 나머지 필드는 내려오지 않을 수 있어 전부 nullable 로 둔다.
 */
data class CourseSaveResponseDto(
    @SerializedName(value = "courseId", alternate = ["savedCourseId"]) val courseId: Long?,
    // 정상값 COMPLETED.
    @SerializedName(value = "draftStatus", alternate = ["status"]) val draftStatus: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("memo") val memo: String?,
    // ISO-8601 LocalDateTime.
    @SerializedName("savedAt") val savedAt: String?,
    // 기준 장소를 포함한 최종 코스의 장소 수.
    @SerializedName("placeCount") val placeCount: Int?,
    // 저장 화면에서 확정한 분위기 태그.
    @SerializedName("moodTags") val moodTags: List<MoodTagSummaryDto>? = null,
    // 임시 코스에서 그대로 복사된 음식 카테고리.
    @SerializedName("foodCategories") val foodCategories: List<FoodCategorySummaryDto>? = null,
    // 최종 코스 장소 목록(visitOrder 오름차순).
    @SerializedName("places") val places: List<SavedCoursePlaceDto>? = null,
)

/** 최종 저장된 코스의 장소 한 건. 저장 시점에는 장소별 메모가 없어 [memo] 는 null 로 내려온다. */
data class SavedCoursePlaceDto(
    // 최종 코스와 장소의 연결 행 ID(course_place.id). 임시 코스의 courseDraftPlaceId 와는 다른 값이다.
    @SerializedName("coursePlaceId") val coursePlaceId: Long,
    @SerializedName("placeId") val placeId: Long,
    // BASE(기준 장소, visitOrder 1) / SELECTED(visitOrder 2 이상).
    @SerializedName("placeRole") val placeRole: String,
    @SerializedName("visitOrder") val visitOrder: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("memo") val memo: String? = null,
)
