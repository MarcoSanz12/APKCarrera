package com.gf.apkcarrera.di

import android.content.Context
import android.content.SharedPreferences
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
    fun providesPreferences(@ApplicationContext context : Context) : SharedPreferences =
        context.getSharedPreferences(context.packageName + "_preferences",Context.MODE_PRIVATE)

}