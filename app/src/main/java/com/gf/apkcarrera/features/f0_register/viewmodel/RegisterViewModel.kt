package com.gf.apkcarrera.features.f0_register.viewmodel

import com.gf.apkcarrera.features.f0_register.usecase.CheckUserExistsUseCase
import com.gf.apkcarrera.features.f0_register.usecase.LoginUseCase
import com.gf.apkcarrera.features.f0_register.usecase.RegisterUseCase
import com.gf.common.entity.user.LoginRequest
import com.gf.common.entity.user.UserModel
import com.gf.common.platform.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val checkUserExistsUseCase: CheckUserExistsUseCase
) : BaseViewModel()  {


    var request: LoginRequest? = null


    fun register(pRequest: LoginRequest,
                 handleOk : (user: UserModel) -> Unit){
        IO.launch {
            registerUseCase(RegisterUseCase.Params(pRequest)){
                it.foldMain(::handleFailure,handleOk)
            }
        }
    }


    fun login(pRequest: LoginRequest,
              handleOk : (user: UserModel) -> Unit){
        IO.launch {
            loginUseCase(LoginUseCase.Params(pRequest)){
                it.foldMain(::handleFailure,handleOk)
            }
        }
    }

    fun checkUserExists(pRequest: LoginRequest,
                        handleOk : (exists : Boolean) -> Unit){
        IO.launch {
            checkUserExistsUseCase(CheckUserExistsUseCase.Params(pRequest)){
                it.foldMain(::handleFailure,handleOk)
            }
        }
    }

}