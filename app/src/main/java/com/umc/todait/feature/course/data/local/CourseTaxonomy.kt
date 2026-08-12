package com.umc.todait.feature.course.data.local

import com.umc.todait.feature.course.data.dto.FoodCategoryDto
import com.umc.todait.feature.course.data.dto.FoodCategoryListResponseDto
import com.umc.todait.feature.course.data.dto.MoodTagDto
import com.umc.todait.feature.course.data.dto.MoodTagListResponseDto

/**
 * 취향 설정 기준 데이터(분위기 태그 · 음식 카테고리)를 **앱이 직접 보유**한다.
 *
 * 명세서상 `GET /api/mood-tags` · `GET /api/food-categories` 는 비고에
 * "프론트엔드에서 담당해주시기로 합의됐습니다"라고 적힌 항목이라 **배포 서버에 엔드포인트가 없다**.
 * 호출하면 핸들러가 없어 COMMON500("내부 서버에 오류가 발생했습니다")이 내려온다.
 * 그래서 목록은 서버에서 받지 않고 여기서 제공한다. (Mock 데이터가 아니라 확정된 기준 데이터다.)
 *
 * ⚠️ [MoodTagDto.moodTagId] · [FoodCategoryDto.foodCategoryId] 는 그대로 선택 저장 API
 * (`PUT /api/course-drafts/{id}/mood-tags`, `.../food-categories`)와 코스 저장 API 의
 * `moodTagIds` 로 전달된다. **서버 mood_tag / food_category 테이블의 id 와 반드시 일치해야 하므로
 * 임의로 바꾸면 저장이 깨진다.** 값은 명세 응답 예시를 그대로 옮긴 것이다.
 *
 * BE 가 조회 API 를 열면 [com.umc.todait.feature.course.data.repository.TaxonomyRepository] 의
 * `USE_SERVER_TAXONOMY` 를 true 로 바꾸면 서버 응답을 그대로 타게 된다.
 */
object CourseTaxonomy {

    /** 분위기 태그 6종. 정렬은 [MoodTagDto.sortOrder] 를 따른다. */
    val moodTags = MoodTagListResponseDto(
        moodTags = listOf(
            MoodTagDto(1, "HIP", "힙한", "트렌디하고 감각적인 분위기", 1),
            MoodTagDto(2, "QUIET", "조용한", "차분하고 대화하기 좋은 분위기", 2),
            MoodTagDto(3, "ACTIVE", "활발한", "밝고 에너지 있는 분위기", 3),
            MoodTagDto(4, "ROMANTIC", "로맨틱", "데이트에 어울리는 감성적인 분위기", 4),
            MoodTagDto(5, "MODERN", "모던한", "깔끔하고 세련된 분위기", 5),
            MoodTagDto(6, "CALM", "차분한", "편안하고 안정적인 분위기", 6),
        ),
    )

    /** 음식 카테고리 6종. 구조와 정렬 규칙은 분위기 태그와 동일하다. */
    val foodCategories = FoodCategoryListResponseDto(
        foodCategories = listOf(
            FoodCategoryDto(1, "KOREAN", "한식", "한식 음식 카테고리", 1),
            FoodCategoryDto(2, "JAPANESE", "일식", "일식 음식 카테고리", 2),
            FoodCategoryDto(3, "WESTERN", "양식", "양식 음식 카테고리", 3),
            FoodCategoryDto(4, "CHINESE", "중식", "중식 음식 카테고리", 4),
            FoodCategoryDto(5, "SNACK", "분식", "분식 음식 카테고리", 5),
            FoodCategoryDto(6, "DESSERT", "디저트", "디저트 음식 카테고리", 6),
        ),
    )
}
