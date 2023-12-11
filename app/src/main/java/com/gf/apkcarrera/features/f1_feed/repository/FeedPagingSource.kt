package com.gf.apkcarrera.features.f1_feed.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cotesa.common.extensions.notNull
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FeedPagingSource(
    val targetId : String?,
    val user : UserModel,
    val firestore : FirebaseFirestore,
    val database : APKCarreraDatabase
) : PagingSource<Int,Pair<ActivityModel,UserModel>>() {

    val cachedFriends = mutableListOf<UserModel>()
    var nextPageNumber : Int = 0
    var lastTimeStamp = (System.currentTimeMillis() / 1000)

    companion object{
        private const val pageCount = 50L
        private const val TAG = "FeedPagingSource"
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pair<ActivityModel,UserModel>> {
        nextPageNumber = params.key ?: 1

        return try {
            val result = if (targetId != null)
                loadFromTarget()
            else
                loadFromUser()

            val nextKey = if (result.size == 50)
                nextPageNumber + 1
            else
                null


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

    private suspend fun loadFromTarget() : List<Pair<ActivityModel,UserModel>>{
        Log.d(TAG, "user activities from : $targetId ")

        val activities = firestore.collection("activities")
            .whereEqualTo("userid",targetId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastTimeStamp)
            .limit(pageCount)
            .get()
            .await()
            .documents.map { ActivityModel(it) }

        lastTimeStamp = activities.last().timestamp

        Log.d(TAG, "target activities -> ${activities.size}")

        // Buscamos al usuario seleccionado
        val targetUser = if (cachedFriends.any {it.uid == targetId})
            cachedFriends.find { it.uid == targetId }!!
        else{
            val foundUser = UserModel(firestore.collection("users").document(targetId!!)
                .get()
                .await())
            cachedFriends.add(foundUser)
            foundUser
        }



        return activities.map { Pair(it,targetUser) }
    }

    private suspend fun loadFromUser() : List<Pair<ActivityModel,UserModel>>{

        val allIds : List<String> = user.friendList + user.uid

        Log.d(TAG, "user activities from [${allIds.size}]: $allIds starting after (${pageCount * nextPageNumber - 1})")

        val activities = firestore.collection("activities")
            .whereIn("userid",allIds)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastTimeStamp)
            .limit(pageCount)
            .get()
            .await()
            .documents.map { ActivityModel(it) }

        lastTimeStamp = activities.last().timestamp

        Log.d(TAG, "user activities -> ${activities.size}")

        val targetUsers = if (cachedFriends.map { it.uid }.containsAll(user.friendList))
            cachedFriends.filter { it.uid in user.friendList }
        else{
            val foundUsers = firestore.collection("users")
                .whereIn(FieldPath.documentId(),user.friendList)
                .whereArrayContains("friendList",user.uid)
                .get()
                .await()
                .documents.map { UserModel(it) }

            foundUsers.forEach {
                if (it !in cachedFriends)
                    cachedFriends.add(it)
            }
            foundUsers
        }


        return activities.map { activity ->
            Pair(activity,
                (targetUsers + user).find { it.uid == activity.userid }!!) }
    }
    override fun getRefreshKey(state: PagingState<Int, Pair<ActivityModel,UserModel>>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }


}