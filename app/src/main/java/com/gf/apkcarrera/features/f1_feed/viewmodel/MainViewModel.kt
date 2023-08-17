package com.gf.apkcarrera.features.f1_feed.viewmodel

import androidx.lifecycle.MutableLiveData
import com.gf.apkcarrera.features.f1_feed.usecase.GetUserUseCase
import com.gf.common.entity.user.UserModel
import com.gf.common.platform.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase
) : BaseViewModel()  {

    var user : MutableLiveData<UserModel> = MutableLiveData()

    fun getUser(){
        IO.launch {
            getUserUseCase(GetUserUseCase.Params("")){
                it.foldMain(::handleFailure,::handleUserLoaded)
            }
        }
    }

    private fun handleUserLoaded(userModel: UserModel){
        user.postValue(userModel)
    }
}