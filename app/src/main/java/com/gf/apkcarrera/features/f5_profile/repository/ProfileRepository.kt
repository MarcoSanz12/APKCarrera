package com.gf.apkcarrera.features.f5_profile.repository

import android.content.Context
import android.content.SharedPreferences
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.gf.common.platform.NetworkHandler
import com.gf.common.response.FriendListResponse
import com.gf.common.response.GenericResponse
import com.gf.common.response.ProfileResponse
import com.gf.common.response.ProfileUpdateResponse
import com.gf.common.utils.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {

    suspend fun getProfile(userId : String?) : ProfileResponse

    suspend fun updateData(userId: String, name : String, image : String) : ProfileUpdateResponse

    @Singleton
    class ProfileRepositoryImpl
    @Inject constructor(
        @ApplicationContext val context: Context,
        val preferences: SharedPreferences,
        val networkHandler: NetworkHandler,
        val database: APKCarreraDatabase,
        val auth: FirebaseAuth,
        val firestore: FirebaseFirestore,
        val analytics: FirebaseAnalytics
    ) : ProfileRepository {

        companion object{
            private const val TAG = "ProfileRepository"
        }
        override suspend fun getProfile (userId: String?): ProfileResponse {

            val uid = userId ?: (preferences.getString(Constants.Login.LOG_UID,null) ?: return ProfileResponse.Error)

            // Si no hay conexión, buscar en local
            if (!networkHandler.isConnected)
                return if (userId == null)
                    getUserDataNoConnection(uid)
                else
                    ProfileResponse.Error

            return try {
                val user = UserModel(firestore.collection("users").document(uid).get().await())
                var activities = firestore.collection("activities").whereEqualTo("userid",uid).get().await().documents.map {
                    ActivityModel(it)
                }

                // Si no es el usuario, filtrar las carrerar privadas
                if (uid == preferences.getString(Constants.Login.LOG_UID,null))
                    activities = activities.filter { it.visibility }


                // Guardamos en ROOM por si acaso
                database.userDao().addUser(user)
                database.activityDao().addActivity(activities)

                ProfileResponse.Succesful(user,activities)
            }catch (ex:Exception){
                ex.printStackTrace()
                ProfileResponse.Error
            }
        }

        override suspend fun updateData(userId: String, name : String, image : String) : ProfileUpdateResponse{
            return try{
               firestore.collection("users").document(userId)
                    .update(mapOf(
                        Pair("name",name),
                        Pair("picture",image)
                    )).await()

                runBlocking {
                    database.userDao().updateProfile(userId,name,image)
                }

                ProfileUpdateResponse.Succesful(name,image)
            }catch (ex:Exception){
                ex.printStackTrace()
                ProfileUpdateResponse.Error
            }
        }


        private suspend fun getUserDataNoConnection (userId: String) : ProfileResponse{
            val user = database.userDao().getUserByUid(userId) ?: return ProfileResponse.Error
            val activities = database.activityDao().getActivitiesByUserId(userId)

            return ProfileResponse.Succesful(user,activities)
        }
        private suspend fun getUser(userId: String) : UserModel? {
            return try {
                val user = UserModel().apply {
                    getModelFromDoc(firestore.collection("users").document(userId).get().await())
                }
                user
            }catch (ex:Exception){
                ex.printStackTrace()
                null
            }

        }


    }


}