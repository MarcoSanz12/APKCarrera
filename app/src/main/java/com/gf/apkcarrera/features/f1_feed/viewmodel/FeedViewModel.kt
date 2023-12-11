package com.gf.apkcarrera.features.f1_feed.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.gf.apkcarrera.features.f1_feed.usecase.GetFeedActivitiesUseCase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.platform.BaseViewModel
import com.gf.common.response.FeedResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeedActivitiesUseCase: GetFeedActivitiesUseCase
) : BaseViewModel()  {

    var user : MutableLiveData<UserModel> = MutableLiveData()

    var _feedFlow : Flow<PagingData<Pair<ActivityModel,UserModel>>>? = null

    fun getFeedActivities(userId : String?) = launch {
        val response = getFeedActivitiesUseCase.invoke(userId,viewModelScope)
        if (response is FeedResponse.Succesful)
            _feedFlow = response.flow
        else
            _failureState.emit(Failure.ServerError)
    }

}