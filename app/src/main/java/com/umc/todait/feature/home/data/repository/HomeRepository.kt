package com.umc.todait.feature.home.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceResultDto
import com.umc.todait.feature.home.data.service.HomeService
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val homeService: HomeService,
) {

    suspend fun getRecommendedPlaces(type: String): ApiResult<HomeRecommendedPlaceResultDto> =
        safeApiCall { homeService.getRecommendedPlaces(type = type) }
}
