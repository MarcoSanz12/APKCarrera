package com.gf.apkcarrera.features.f4_settings.usecase

import com.gf.apkcarrera.repository.RegisterRepository
import com.gf.common.exception.Failure
import com.gf.common.functional.Either
import com.gf.common.platform.UseCase
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val registerRepository: RegisterRepository) : UseCase<Any, LogoutUseCase.Params>() {

    data class Params(val any:Any)

    override suspend fun run(params: Params): Either<Failure, Any> = registerRepository.logout()

}