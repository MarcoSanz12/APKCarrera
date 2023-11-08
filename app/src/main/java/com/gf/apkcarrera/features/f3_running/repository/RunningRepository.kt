package com.gf.apkcarrera.features.f3_running.repository

import android.content.Context
import com.gf.apkcarrera.repository.MainRepository
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.functional.Either
import com.gf.common.platform.NetworkHandler
import com.gf.common.response.UploadActivityResponse
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface RunningRepository {

    suspend fun saveActivity(activityModel: ActivityModel) : UploadActivityResponse

    @Singleton
    class RunningRepositoryImpl
    @Inject constructor(
        @ApplicationContext val context: Context,
        val networkHandler: NetworkHandler,
        val database: APKCarreraDatabase,
        val auth: FirebaseAuth,
        val firestore: FirebaseFirestore,
        val analytics: FirebaseAnalytics
    ) : RunningRepository {
        override suspend fun saveActivity(activityModel: ActivityModel): UploadActivityResponse{
            if (!networkHandler.isConnected)
                return UploadActivityResponse.Error
            runCatching {
                firestore.collection("activities").add(activityModel).await()
            }.fold(
                onSuccess = {
                    return UploadActivityResponse.Succesful
                },
                onFailure = {
                    return UploadActivityResponse.Error
                }
            )
        }
    }
}