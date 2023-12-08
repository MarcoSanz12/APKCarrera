package com.gf.apkcarrera.features.f1_feed.usecase

import com.gf.apkcarrera.features.f1_feed.repository.FeedRepository
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.functional.Either
import com.gf.common.platform.UseCase
import javax.inject.Inject

class GetUserUseCase @Inject constructor (private val feedRepository: FeedRepository) : UseCase<UserModel, GetUserUseCase.Params>() {

    data class Params(val none: Any)

    override suspend fun run(params: Params): Either<Failure, UserModel> = feedRepository.getUserData()

}