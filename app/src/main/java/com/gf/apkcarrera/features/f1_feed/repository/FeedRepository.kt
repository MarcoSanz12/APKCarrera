package com.gf.apkcarrera.features.f1_feed.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.functional.Either
import com.gf.common.platform.NetworkHandler
import com.gf.common.response.FeedResponse
import com.gf.common.utils.Constants.Login.LOG_UID
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

interface FeedRepository {

    suspend fun getUserData() : Either<Failure,UserModel>

    suspend fun getFeedActivities(scope: CoroutineScope) : FeedResponse

    @Singleton
    class MainRepositoryImpl
    @Inject constructor(
        @ApplicationContext val context: Context,
        val networkHandler: NetworkHandler,
        val database: APKCarreraDatabase,
        val auth: FirebaseAuth,
        val firestore: FirebaseFirestore,
        val analytics: FirebaseAnalytics
    ) : FeedRepository {

           val preferences = context.getSharedPreferences(
            context.packageName + "_preferences",
            Context.MODE_PRIVATE
        )

        private val userId by lazy { preferences.getString(LOG_UID,"")}

        private val user by lazy {
            runBlocking {
                database.userDao().getUserByUid(userId!!)
            }}

        override suspend fun getUserData(): Either<Failure, UserModel> {
            if (!networkHandler.isConnected)
                return Either.Left(Failure.ServerError)

            return try{
                val uid = preferences.getString(LOG_UID,"")
                val user = database.userDao().getUserByUid(uid!!) ?: return Either.Left(Failure.ServerError)
                Either.Right(user)

            }catch (ex:Throwable){
                ex.printStackTrace()
                Either.Left(Failure.ServerError)
            }
        }

        override suspend fun getFeedActivities(scope : CoroutineScope): FeedResponse{
            user ?: return FeedResponse.Error

            val flow = Pager(
                // Configure how data is loaded by passing additional properties to
                // PagingConfig, such as prefetchDistance.
                PagingConfig(pageSize = 50)
            ) {
                FeedPagingSource(userId!!, user!!.friendList, firestore, database)
            }.flow
                .cachedIn(scope)

            return FeedResponse.Succesful(flow)
        }
    }
}