package com.gf.apkcarrera.features.f5_profile.usecase

import com.gf.apkcarrera.features.f5_profile.repository.ProfileRepository
import com.gf.common.response.ProfileResponse
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(userId : String?) : ProfileResponse = profileRepository.getProfile(userId)
}