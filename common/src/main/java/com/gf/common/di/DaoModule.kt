package com.gf.common.di

import android.content.Context
import androidx.room.Room
import com.gf.common.db.APKCarreraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DaoModule {
    companion object{
        const val DATABASE_NAME = "APKCarrera_database"
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): APKCarreraDatabase {
        return Room.databaseBuilder(
            context, APKCarreraDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideUserDao(db:APKCarreraDatabase) = db.userDao()
}