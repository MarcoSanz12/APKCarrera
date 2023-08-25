package com.gf.apkcarrera.di

import com.gf.apkcarrera.repository.RegisterRepository
import com.gf.apkcarrera.repository.MainRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationModule {

    @Binds
    @Singleton
    abstract fun bindRegisterRepository(repository: RegisterRepository.RegisterRepositoryImpl) : RegisterRepository

    @Binds
    @Singleton
    abstract fun bindMainRepository(repository: MainRepository.MainRepositoryImpl) : MainRepository
}