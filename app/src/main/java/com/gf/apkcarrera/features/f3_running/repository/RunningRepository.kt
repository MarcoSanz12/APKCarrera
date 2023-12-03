package com.gf.apkcarrera.features.f3_running.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.platform.NetworkHandler
import com.gf.common.response.GenericResponse
import com.gf.common.utils.Constants.Login.LOG_UID
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface RunningRepository {

    suspend fun saveActivity(activityModel: ActivityModel) : GenericResponse

    @Singleton
    class RunningRepositoryImpl
    @Inject constructor(
        @ApplicationContext val context: Context,
        val preferences: SharedPreferences,
        val networkHandler: NetworkHandler,
        val database: APKCarreraDatabase,
        val auth: FirebaseAuth,
        val firestore: FirebaseFirestore,
        val analytics: FirebaseAnalytics
    ) : RunningRepository {

        companion object{
            private const val TAG = "RunningRepository"
        }
        override suspend fun saveActivity(activityModel: ActivityModel): GenericResponse{
            if (!networkHandler.isConnected)
                return GenericResponse.Error

            val userId = preferences.getString(LOG_UID,null) ?: return GenericResponse.Error

            activityModel.userid = userId

            var response : GenericResponse = GenericResponse.Succesful

            firestore.runTransaction {
                firestore.collection("activities").document().set(activityModel.setModelToMap())
                firestore.collection("users").document(userId).update("lastActivity",activityModel.timestamp)
            }.addOnSuccessListener {
                response = GenericResponse.Succesful
            }.addOnFailureListener {
                Log.e(TAG,it.stackTraceToString())
                response = GenericResponse.Error
            }

            return response

        }
    }
}