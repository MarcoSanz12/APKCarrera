package com.gf.apkcarrera.features.f1_feed.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.gf.apkcarrera.features.f1_feed.repository.FeedPagingSource
import com.gf.apkcarrera.features.f1_feed.usecase.GetFeedActivitiesUseCase
import com.gf.apkcarrera.features.f1_feed.usecase.GetUserUseCase
import com.gf.apkcarrera.features.f4_settings.usecase.LogoutUseCase
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.functional.Either
import com.gf.common.platform.BaseViewModel
import com.gf.common.response.FeedResponse
import com.gf.common.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getFeedActivitiesUseCase: GetFeedActivitiesUseCase
) : BaseViewModel()  {

    var user : MutableLiveData<UserModel> = MutableLiveData()

    private val _feedState = MutableStateFlow<FeedResponse?> (null)
    val feedState = _feedState.asStateFlow()

    fun getFeedActivities() = launch {
        _feedState.emit(getFeedActivitiesUseCase.invoke(viewModelScope))
    }

    fun getUser(){
        IO.launch {
            getUserUseCase(GetUserUseCase.Params("")){
                it.foldMain(::handleFailure,::handleUserLoaded)
            }
        }
    }

    fun logout(handleDone : () -> Unit){
        IO.launch {
            logoutUseCase(LogoutUseCase.Params("")){
                it.foldMain(
                    fnL = {
                        handleDone.invoke()
                    },
                    fnR = {
                        handleDone.invoke()
                    }
                )
            }
        }
    }

    private fun handleUserLoaded(userModel: UserModel){
        user.postValue(userModel)
    }
}