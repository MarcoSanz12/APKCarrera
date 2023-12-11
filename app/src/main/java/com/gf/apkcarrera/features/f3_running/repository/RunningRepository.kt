package com.gf.apkcarrera.features.f3_running.repository

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.platform.NetworkHandler
import com.gf.common.response.GenericResponse
import com.gf.common.utils.Constants.Login.LOG_UID
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface RunningRepository {

    suspend fun saveActivity(activityModel: ActivityModel,imagesBitmap : List<Bitmap>) : GenericResponse

    @Singleton
    class RunningRepositoryImpl
    @Inject constructor(
        @ApplicationContext val context: Context,
        val preferences: SharedPreferences,
        val networkHandler: NetworkHandler,
        val database: APKCarreraDatabase,
        val auth: FirebaseAuth,
        val firestore: FirebaseFirestore,
        val analytics: FirebaseAnalytics,
        val fireimages : FirebaseStorage
    ) : RunningRepository {

        companion object{
            private const val TAG = "RunningRepository"
        }
        override suspend fun saveActivity(activityModel: ActivityModel, imagesBitmap: List<Bitmap>): GenericResponse{
            if (!networkHandler.isConnected)
                return GenericResponse.Error

            val userId = preferences.getString(LOG_UID,null) ?: return GenericResponse.Error

            activityModel.userid = userId

            var response : GenericResponse = GenericResponse.Succesful

            try{



                val imageNames = mutableListOf<String>()
                for (i in 1..imagesBitmap.size){
                    imageNames.add("${activityModel.userid}_${activityModel.timestamp}_${i}.jpg")
                }

                var downloadUrls = mutableListOf<String>()
                imageNames.forEach {name->
                    val imageRef = fireimages.reference.child("images/$name")
                    val position = imageNames.indexOf(name)

                    val baos = ByteArrayOutputStream()
                    imagesBitmap[position].compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    val response = imageRef.putBytes(data).await()
                    if (response.task.isSuccessful)
                        downloadUrls.add(imageRef.downloadUrl.await().toString())

                }

                firestore.runTransaction {
                    activityModel.images = downloadUrls.toList()
                    firestore.collection("activities").document().set(activityModel)
                    firestore.collection("users").document(userId).update("lastActivity",activityModel.timestamp)

                }.addOnSuccessListener {
                    response = GenericResponse.Succesful
                }.addOnFailureListener {
                    Log.e(TAG,it.stackTraceToString())
                    response = GenericResponse.Error
                }.await()

                return response
            }catch (ex:Exception){
                return GenericResponse.Error
            }

        }
    }
}