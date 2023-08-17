package com.gf.apkcarrera.features.f0_register.usecase

import com.gf.apkcarrera.features.f0_register.RegisterRepository
import com.gf.common.entity.user.LoginRequest
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.functional.Either
import com.gf.common.platform.UseCase
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val registerRepository: RegisterRepository) : UseCase<UserModel, LoginUseCase.Params>() {

    data class Params(val user : LoginRequest)

    override suspend fun run(params: Params): Either<Failure, UserModel> = registerRepository.login(params.user)

}