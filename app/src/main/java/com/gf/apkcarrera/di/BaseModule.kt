package com.gf.apkcarrera.di

import android.content.Context
import com.gf.apkcarrera.features.f3_activity.viewmodel.ActivityViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BaseModule {
    @Provides
    @Singleton
    fun providesActivityViewModel() : ActivityViewModel = ActivityViewModel()

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideLocationRequest() : LocationRequest = LocationRequest.Builder( Priority.PRIORITY_HIGH_ACCURACY,1000).build()

}