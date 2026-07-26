package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "임시 코스 생성"(POST /api/course-drafts)의 result. (BaseResponse<CourseDraftCreateResponseDto>)
 *
 * 코스 생성 진입 시 발급되는 임시 코스(course-draft) 핸들. 이후 기준/선택 장소 저장·순서 변경·
 * 최종 저장 API 가 모두 이 [courseDraftId] 를 경로 변수로 사용한다.
 *
 * ⚠️ 배포 스펙 기준. 요청 바디는 없다(로그인 사용자 기준으로 서버가 생성).
 */
data class CourseDraftCreateResponseDto(
    @SerializedName("courseDraftId") val courseDraftId: Long,
    // 생성 직후 정상값은 MOOD_SELECTING.
    // (DB/Entity 는 status 지만 API JSON 은 임시 코스 상태임을 분명히 하려고 draftStatus 를 쓴다.)
    @SerializedName("draftStatus") val draftStatus: String,
    // 만료 정책 미확정 — 명세상 응답에서 빠지거나 null 로 올 수 있다.
    @SerializedName("expiresAt") val expiresAt: String?,
    // ISO-8601 LocalDateTime.
    @SerializedName("createdAt") val createdAt: String,
)
