package com.umc.todait.di

import com.umc.todait.feature.saved.data.service.SavedService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SavedModule {

    @Provides
    @Singleton
    fun provideSavedService(
        retrofit: Retrofit,
    ): SavedService =
        retrofit.create(SavedService::class.java)
}