package com.gf.apkcarrera.features.f1_feed.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.gf.apkcarrera.features.f1_feed.usecase.GetFeedActivitiesUseCase
import com.gf.apkcarrera.features.f1_feed.usecase.GetUserUseCase
import com.gf.apkcarrera.features.f4_settings.usecase.LogoutUseCase
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.platform.BaseViewModel
import com.gf.common.response.FeedResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeedActivitiesUseCase: GetFeedActivitiesUseCase
) : BaseViewModel()  {

    var user : MutableLiveData<UserModel> = MutableLiveData()

    var _feedFlow : Flow<PagingData<ActivityModel>>? = null

    fun getFeedActivities() = launch {
        val response = getFeedActivitiesUseCase.invoke(viewModelScope)
        if (response is FeedResponse.Succesful)
            _feedFlow = response.flow
        else
            _failureState.emit(Failure.ServerError)
    }

}