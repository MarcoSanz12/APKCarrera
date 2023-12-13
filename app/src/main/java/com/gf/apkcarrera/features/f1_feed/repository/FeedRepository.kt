package com.gf.apkcarrera.features.f1_feed.repository

import android.content.Context
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

    suspend fun getFeedActivities(targetId : String?, scope: CoroutineScope) : FeedResponse

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

        override suspend fun getFeedActivities(targetId: String?, scope : CoroutineScope): FeedResponse{

            var flow : Flow<PagingData<Pair<ActivityModel,UserModel>>>? = null
            runBlocking {
                val userId = preferences.getString(LOG_UID,"")  ?: return@runBlocking FeedResponse.Error
                val user = database.userDao().getUserByUid(userId)
                user ?: return@runBlocking FeedResponse.Error


                flow = Pager(
                    // Configure how data is loaded by passing additional properties to
                    // PagingConfig, such as prefetchDistance.
                    PagingConfig(pageSize = 50)
                ) {
                    FeedPagingSource(targetId, user, firestore, database)
                }.flow
                    .cachedIn(scope)

            }

            return if (flow != null)
                FeedResponse.Succesful(flow!!)
            else
                FeedResponse.Error
        }
    }
}