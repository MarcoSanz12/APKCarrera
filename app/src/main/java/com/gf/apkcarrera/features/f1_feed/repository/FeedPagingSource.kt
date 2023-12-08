package com.gf.apkcarrera.features.f1_feed.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.response.FeedResponse
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FeedPagingSource(
    val userId : String,
    val friendsIds : List<String>,
    val firestore : FirebaseFirestore,
    val database : APKCarreraDatabase
) : PagingSource<Int,ActivityModel>() {

    companion object{
        private const val pageCount = 50L
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ActivityModel> {
        val nextPageNumber = params.key ?: 1

        val allIds = friendsIds.toMutableList().apply{
            add(userId)
        }

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

            val nextKey = if (friendActivities.size == 50)
                nextPageNumber + 1
            else
                null


            LoadResult.Page(
                data = friendActivities,
                prevKey = null,
                nextKey = nextKey
            )
        }catch (ex:Exception){
            ex.printStackTrace()
            LoadResult.Error(ex)
        }


    }
    override fun getRefreshKey(state: PagingState<Int, ActivityModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }


}