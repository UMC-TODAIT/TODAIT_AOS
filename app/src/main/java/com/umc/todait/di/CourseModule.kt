package com.umc.todait.di

import com.umc.todait.feature.course.data.service.SearchService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * course feature 전용 Hilt 모듈.
 *
 * Retrofit 인터페이스(Service)는 직접 생성할 수 없어 retrofit.create() 레시피를 여기서 제공한다.
 * (Retrofit/OkHttp 등 공통 인프라는 NetworkModule 에서 제공)
 * Repository/ViewModel 은 @Inject 생성자로 자동 주입되므로 별도 등록이 필요 없다.
 */
@Module
@InstallIn(SingletonComponent::class)
object CourseModule {

    @Provides
    @Singleton
    fun provideSearchService(retrofit: Retrofit): SearchService =
        retrofit.create(SearchService::class.java)
}
