package com.gf.common.response

import androidx.paging.PagingData
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.gf.common.states.ProfileState
import kotlinx.coroutines.flow.Flow

sealed class FeedResponse {

    class Succesful(
        val flow : Flow<PagingData<Pair<ActivityModel, UserModel>>>
    ) : FeedResponse()

    data object Error : FeedResponse()

    fun fold(
        onSuccess: (flow: Flow<PagingData<Pair<ActivityModel,UserModel>>>) -> Unit,
        onError: () -> Unit
    ) {
        when (this) {
            is Succesful -> onSuccess(flow)
            is Error -> onError()
        }
    }
}