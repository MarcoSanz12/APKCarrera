package com.gf.apkcarrera.features.f0_register.usecase

import com.gf.apkcarrera.repository.RegisterRepository
import com.gf.common.entity.user.LoginRequest
import com.gf.common.exception.Failure
import com.gf.common.functional.Either
import com.gf.common.platform.UseCase
import javax.inject.Inject

class CheckUserExistsUseCase @Inject constructor(private val registerRepository: RegisterRepository) : UseCase<Boolean, CheckUserExistsUseCase.Params>() {

    data class Params(val user : LoginRequest)

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        return Either.Right(registerRepository.checkUserExists(params.user))
    }
}