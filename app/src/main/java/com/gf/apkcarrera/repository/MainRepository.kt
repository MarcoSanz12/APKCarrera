package com.gf.apkcarrera.repository

import android.content.Context
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.functional.Either
import com.gf.common.platform.NetworkHandler
import com.gf.common.utils.Constants.Login.LOG_UID
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface MainRepository {

    suspend fun getUserData() : Either<Failure,UserModel>

    @Singleton
    class MainRepositoryImpl
    @Inject constructor(
        @ApplicationContext val context: Context,
        val networkHandler: NetworkHandler,
        val database: APKCarreraDatabase,
        val auth: FirebaseAuth,
        val firestore: FirebaseFirestore,
        val analytics: FirebaseAnalytics
    ) : MainRepository {

           val preferences = context.getSharedPreferences(
            context.packageName + "_preferences",
            Context.MODE_PRIVATE
        )

        override suspend fun getUserData(): Either<Failure, UserModel> {
            if (!networkHandler.isConnected)
                return Either.Left(Failure.ServerError)

            return try{
                val uid = preferences.getString(LOG_UID,"")
                val user = database.userDao().getUserByUid(uid!!)
                Either.Right(user)

            }catch (ex:Throwable){
                ex.printStackTrace()
                Either.Left(Failure.ServerError)
            }
        }
    }
}