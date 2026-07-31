package com.umc.todait.feature.home.data.mock

import com.umc.todait.core.mock.MockImages
import com.umc.todait.feature.home.data.dto.CourseTagDto
import com.umc.todait.feature.home.data.dto.HomeAreaDto
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceDto
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceResultDto
import com.umc.todait.feature.home.data.dto.MoodTagDto
import com.umc.todait.feature.home.data.dto.PlaceCategoryDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseDetailDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseListResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCoursePlaceDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseSaveResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseSummaryDto

/**
 * 홈 화면 Mock 데이터 (MVP 시연용). HomeRepository 의 USE_MOCK=true 일 때 반환한다.
 * 서버 없이도 피그마 디자인대로 "오늘의 추천 코스" / "취향 기반 추천 장소" / 추천 코스 상세 화면이 채워진다.
 * 이미지는 피그마 예시 사진을 담은 [MockImages] 의 asset 경로를 쓴다.
 * 필드 형태는 "JSON 필드 사전 v1.0" 기준 실 API 응답과 동일하게 맞춰서, USE_MOCK 을 false 로 바꾸기만 하면
 * 실 API 연동으로 전환된다.
 */
object MockHome {

    private val YEONNAM = HomeAreaDto(areaId = 2, code = "YEONNAM", name = "연남")
    private val HONGDAE = HomeAreaDto(areaId = 1, code = "HONGDAE", name = "홍대")
    private val SEONGSU = HomeAreaDto(areaId = 3, code = "SEONGSU", name = "성수")

    /** GET /api/recommended-courses (page=0, size=3) mock 응답. */
    val courses = RecommendedCourseListResultDto(
        recommendationLogId = 410,
        page = 0,
        size = 3,
        totalElements = 18,
        totalPages = 6,
        hasNext = true,
        courses = listOf(
            RecommendedCourseSummaryDto(
                courseId = 1,
                title = "연남 데이트 코스",
                area = YEONNAM,
                representativeImageUrl = MockImages.CAFE_MATCHA_LATTE,
                tags = listOf(
                    CourseTagDto(type = "MOOD", code = "ROMANTIC", name = "로맨틱"),
                    CourseTagDto(type = "SUB_CATEGORY", code = null, name = "베이커리카페"),
                ),
                placeCount = 4,
                rank = 1,
                detailAvailable = true,
            ),
            RecommendedCourseSummaryDto(
                courseId = 2,
                title = "홍대 감성 데이트 코스",
                area = HONGDAE,
                representativeImageUrl = MockImages.BAR_COCKTAIL,
                tags = listOf(
                    CourseTagDto(type = "MOOD", code = "HIP", name = "힙한"),
                    CourseTagDto(type = "SUB_CATEGORY", code = null, name = "칵테일바"),
                ),
                placeCount = 4,
                rank = 2,
                detailAvailable = true,
            ),
            RecommendedCourseSummaryDto(
                courseId = 3,
                title = "성수 모던 브런치 코스",
                area = SEONGSU,
                representativeImageUrl = MockImages.CAFE_ICED_COFFEE,
                tags = listOf(
                    CourseTagDto(type = "MOOD", code = "MODERN", name = "모던"),
                    CourseTagDto(type = "SUB_CATEGORY", code = null, name = "브런치"),
                ),
                placeCount = 4,
                rank = 3,
                detailAvailable = true,
            ),
        ),
    )

    /** GET /api/recommended-courses/{courseId} mock 응답. 목록 mock 과 같은 courseId·타이틀을 쓴다. */
    private val courseDetails: Map<Long, RecommendedCourseDetailDto> = listOf(
        RecommendedCourseDetailDto(
            courseId = 1,
            title = "연남 데이트 코스",
            representativeMoodTag = MoodTagDto(moodTagId = 4, code = "ROMANTIC", name = "낭만적인"),
            representativePlaceCategory = PlaceCategoryDto(code = "BAKERY_CAFE", name = "베이커리 카페"),
            placeCount = 4,
            places = listOf(
                RecommendedCoursePlaceDto(101, 21, 1, "더 파이브올스", MockImages.RESTAURANT_KOREAN, "서울 마포구 와우산로13길 40", 37.5521, 126.9214),
                RecommendedCoursePlaceDto(102, 22, 2, "연남 카페", MockImages.CAFE_MATCHA_LATTE, "서울 마포구 동교로 241", 37.5612, 126.9248),
                RecommendedCoursePlaceDto(103, 23, 3, "연남 공방", MockImages.ACTIVITY_WORKSHOP, "서울 마포구 성미산로 152", 37.5623, 126.9259),
                RecommendedCoursePlaceDto(104, 24, 4, "연남 와인바", MockImages.BAR_COCKTAIL, "서울 마포구 동교로38길 27", 37.5631, 126.9272),
            ),
        ),
        RecommendedCourseDetailDto(
            courseId = 2,
            title = "홍대 감성 데이트 코스",
            representativeMoodTag = MoodTagDto(moodTagId = 1, code = "HIP", name = "힙한"),
            representativePlaceCategory = PlaceCategoryDto(code = "COCKTAIL_BAR", name = "칵테일바"),
            placeCount = 3,
            places = listOf(
                RecommendedCoursePlaceDto(201, 31, 1, "홍대 클럽샌드", MockImages.RESTAURANT_KOREAN, "서울 마포구 와우산로 29길 6", 37.5524, 126.9231),
                RecommendedCoursePlaceDto(202, 32, 2, "산울림소극장 골목", MockImages.SHOP_STOREFRONT, "서울 마포구 잔다리로 8길 18", 37.5502, 126.9214),
                RecommendedCoursePlaceDto(203, 33, 3, "홍대 루프탑바", MockImages.BAR_COCKTAIL, "서울 마포구 어울마당로 155", 37.5537, 126.9257),
            ),
        ),
        RecommendedCourseDetailDto(
            courseId = 3,
            title = "성수 모던 브런치 코스",
            representativeMoodTag = MoodTagDto(moodTagId = 5, code = "MODERN", name = "모던한"),
            representativePlaceCategory = PlaceCategoryDto(code = "BRUNCH", name = "브런치"),
            placeCount = 3,
            places = listOf(
                RecommendedCoursePlaceDto(301, 41, 1, "성수 브런치클럽", MockImages.CAFE_ICED_COFFEE, "서울 성동구 성수이로 12길 8", 37.5443, 127.0557),
                RecommendedCoursePlaceDto(302, 42, 2, "성수 디자인 전시관", MockImages.ACTIVITY_WORKSHOP, "서울 성동구 성수이로 20", 37.5445, 127.0559),
                RecommendedCoursePlaceDto(303, 43, 3, "언더스탠드에비뉴", MockImages.SHOP_STOREFRONT, "서울 성동구 왕십리로 63", 37.5427, 127.0475),
            ),
        ),
    ).associateBy { it.courseId }

    /** 목록에 없는 courseId 로 접근한 경우를 대비한 기본값(첫 번째 코스). */
    fun courseDetail(courseId: Long): RecommendedCourseDetailDto =
        courseDetails[courseId] ?: courseDetails.getValue(1L)

    /** POST /api/recommended-courses/{courseId}/save mock 응답. */
    fun saveResult(courseId: Long): RecommendedCourseSaveResultDto {
        val detail = courseDetail(courseId)
        return RecommendedCourseSaveResultDto(
            sourceCourseId = courseId,
            savedCourseId = 9000 + courseId,
            title = detail.title,
            visibility = "PRIVATE",
            sourceType = "SERVICE_CREATED",
            placeCount = detail.placeCount,
            savedAt = "2026-07-25T12:00:00",
        )
    }

    /** GET /api/recommended-places (size=2) mock 응답. */
    val places = HomeRecommendedPlaceResultDto(
        recommendationLogId = 510,
        page = 0,
        size = 2,
        locationAvailable = true,
        places = listOf(
            HomeRecommendedPlaceDto(
                placeId = 1,
                name = "어니언 성수",
                address = "서울 성동구 아차산로9길 8",
                roadAddress = "서울 성동구 아차산로9길 8",
                latitude = 37.5443,
                longitude = 127.0557,
                area = SEONGSU,
                category = PlaceCategoryDto(placeCategoryId = 2, code = "CAFE", name = "카페"),
                subCategory = "베이커리카페",
                imageUrl = MockImages.CAFE_MATCHA_LATTE,
                rank = 1,
                distanceMeters = 320,
                isNearby = true,
                recommendationReason = "현재 위치와 가까워요.",
                detailAvailable = true,
            ),
            HomeRecommendedPlaceDto(
                placeId = 2,
                name = "연남동 감성 카페",
                address = "서울 마포구 성미산로 161-4",
                roadAddress = "서울 마포구 성미산로 161-4",
                latitude = 37.5623,
                longitude = 126.9214,
                area = YEONNAM,
                category = PlaceCategoryDto(placeCategoryId = 2, code = "CAFE", name = "카페"),
                subCategory = "카페",
                imageUrl = MockImages.CAFE_ICED_COFFEE,
                rank = 2,
                distanceMeters = null,
                isNearby = null,
                recommendationReason = "연남 추천 장소예요.",
                detailAvailable = true,
            ),
        ),
    )
}
