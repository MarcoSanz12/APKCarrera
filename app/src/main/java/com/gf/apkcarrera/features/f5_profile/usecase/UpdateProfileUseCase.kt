package com.gf.apkcarrera.features.f5_profile.usecase

import com.gf.apkcarrera.features.f5_profile.repository.ProfileRepository
import com.gf.common.response.GenericResponse
import com.gf.common.response.ProfileResponse
import com.gf.common.response.ProfileUpdateResponse
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(userId : String, name : String, image : String) : ProfileUpdateResponse = profileRepository.updateData(userId,name,image)
}