package com.gf.apkcarrera.features.f1_feed.viewmodel

import androidx.lifecycle.MutableLiveData
import com.gf.apkcarrera.features.f1_feed.usecase.GetUserUseCase
import com.gf.apkcarrera.features.f4_settings.usecase.LogoutUseCase
import com.gf.common.entity.user.UserModel
import com.gf.common.platform.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel()  {

    var user : MutableLiveData<UserModel> = MutableLiveData()

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