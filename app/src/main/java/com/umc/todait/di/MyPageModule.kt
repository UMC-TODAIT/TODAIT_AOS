package com.umc.todait.di

import com.umc.todait.feature.mypage.data.service.MyPageService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MyPageModule {
    @Provides
    @Singleton
    fun provideMyPageService(retrofit: Retrofit): MyPageService =
        retrofit.create(MyPageService::class.java)
}