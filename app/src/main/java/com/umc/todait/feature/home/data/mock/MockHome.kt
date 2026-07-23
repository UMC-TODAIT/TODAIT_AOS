package com.umc.todait.feature.home.data.mock

import com.umc.todait.feature.home.data.dto.HomeAreaDto
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceDto
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceResultDto
import com.umc.todait.feature.home.data.dto.MoodTagDto
import com.umc.todait.feature.home.data.dto.PlaceCategoryDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseListResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseSummaryDto

/**
 * 홈 화면 Mock 데이터 (MVP 시연용). HomeRepository 의 USE_MOCK=true 일 때 반환한다.
 * 서버 없이도 피그마 디자인대로 "오늘의 추천 코스" / "취향 기반 추천 장소" 섹션이 채워진다.
 * 이미지 URL 은 없이(null) 두어, 분위기별 그라디언트+문양 카드로 렌더된다.
 */
object MockHome {

    /** GET /api/recommended-courses (page=0, size=3) mock 응답. */
    val courses = RecommendedCourseListResultDto(
        courses = listOf(
            RecommendedCourseSummaryDto(
                courseId = 1,
                title = "홍대 감성 데이트 코스",
                representativeImageUrl = null,
                representativeMoodTag = MoodTagDto(moodTagId = 1, code = "ROMANTIC", name = "로맨틱"),
                representativePlaceCategory = PlaceCategoryDto(code = "CAFE", name = "카페"),
            ),
            RecommendedCourseSummaryDto(
                courseId = 2,
                title = "연남 힙한 골목 코스",
                representativeImageUrl = null,
                representativeMoodTag = MoodTagDto(moodTagId = 2, code = "HIP", name = "힙한"),
                representativePlaceCategory = PlaceCategoryDto(code = "RESTAURANT", name = "맛집"),
            ),
            RecommendedCourseSummaryDto(
                courseId = 3,
                title = "성수 모던 브런치 코스",
                representativeImageUrl = null,
                representativeMoodTag = MoodTagDto(moodTagId = 3, code = "MODERN", name = "모던"),
                representativePlaceCategory = PlaceCategoryDto(code = "BRUNCH", name = "브런치"),
            ),
        ),
        page = 0,
        size = 3,
        totalElements = 3,
        totalPages = 1,
        hasNext = false,
    )

    /** GET /api/recommended-places (size=2) mock 응답. */
    val places = HomeRecommendedPlaceResultDto(
        places = listOf(
            HomeRecommendedPlaceDto(
                placeId = 1,
                name = "어니언 성수",
                address = "서울 성동구 아차산로9길 8",
                representativeImageUrl = null,
                imageType = "DEFAULT",
                area = HomeAreaDto(areaId = 1, code = "SEONGSU", name = "성수"),
                distance = 320,
                isNearby = true,
                recommendReason = "현재 위치와 가까워요",
            ),
            HomeRecommendedPlaceDto(
                placeId = 2,
                name = "연남동 감성 카페",
                address = "서울 마포구 성미산로 161-4",
                representativeImageUrl = null,
                imageType = "DEFAULT",
                area = HomeAreaDto(areaId = 2, code = "YEONNAM", name = "연남"),
                distance = null,
                isNearby = false,
                recommendReason = "연남 추천 장소예요",
            ),
        ),
        locationApplied = true,
        page = 0,
        size = 2,
        totalElements = 2,
        totalPages = 1,
        hasNext = false,
    )
}
