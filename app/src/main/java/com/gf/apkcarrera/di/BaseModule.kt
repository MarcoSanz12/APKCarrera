package com.gf.apkcarrera.di

import com.gf.apkcarrera.features.f3_running.viewmodel.ActivityViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BaseModule {
    @Provides
    @Singleton
    fun providesActivityViewModel() : ActivityViewModel = ActivityViewModel()

}