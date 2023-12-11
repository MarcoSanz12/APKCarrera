package com.gf.apkcarrera.features.f1_feed.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FeedPagingSource(
    val targetId : String?,
    val userId : String,
    val friendsIds : List<String>,
    val firestore : FirebaseFirestore,
    val database : APKCarreraDatabase
) : PagingSource<Int,Pair<ActivityModel,UserModel>>() {

    val cachedFriends = mutableListOf<UserModel>()

    companion object{
        private const val pageCount = 50L
        private const val TAG = "FeedPagingSource"
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pair<ActivityModel,UserModel>> {
        val nextPageNumber = params.key ?: 1

        val allIds : List<String> = if (targetId == null)
            friendsIds.toMutableList().apply{
                add(userId)
            }
        else
            listOf(targetId)

        Log.d(TAG, "Actividades de amigos[${allIds.size}]: ${allIds}")

        return try {
            val friendActivities = firestore.collection("activities")
                .whereIn("userid", allIds )
                .whereNotEqualTo("userid", "")
                .orderBy("userid")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(pageCount)
                .startAfter(pageCount * nextPageNumber)
                .get()
                .await()
                .documents.map { ActivityModel(it) }

            // Si están todos los usuarios en cache, no sacarlos
            if (!cachedFriends.map { it.uid }.containsAll(friendActivities.map { it.userid }) ){
                val friendList = firestore.collection("users")
                    .whereIn(FieldPath.documentId(),friendActivities.map { it.userid })
                    .limit(pageCount)
                    .get()
                    .await()
                    .documents.map { UserModel(it) }

                friendList.forEach {
                    if (!cachedFriends.contains(it))
                        cachedFriends.add(it)
                }
            }

            val nextKey = if (friendActivities.size == 50)
                nextPageNumber + 1
            else
                null

            var result : MutableList<Pair<ActivityModel,UserModel>> = mutableListOf()
            friendActivities.onEach {activity ->
                result.add(Pair(activity,cachedFriends.find { it.uid == activity.userid }!!))
            }


            Log.d(TAG, "Emitiendo pagingData\nResultados [${result.size}]")
            LoadResult.Page(
                data = result,
                prevKey = null,
                nextKey = nextKey
            )
        }catch (ex:Exception){
            ex.printStackTrace()
            LoadResult.Error(ex)
        }


    }
    override fun getRefreshKey(state: PagingState<Int, Pair<ActivityModel,UserModel>>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }


}