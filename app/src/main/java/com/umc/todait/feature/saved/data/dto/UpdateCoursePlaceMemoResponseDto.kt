package com.umc.todait.feature.saved.data.dto

data class UpdateCoursePlaceMemoResponseDto(
    val courseId: Long,
    val coursePlaceId: Long,
    val memo: String?
)