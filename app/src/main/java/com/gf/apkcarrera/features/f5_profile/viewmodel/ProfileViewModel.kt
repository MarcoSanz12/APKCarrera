package com.gf.apkcarrera.features.f5_profile.viewmodel

import androidx.lifecycle.viewModelScope
import com.gf.apkcarrera.features.f1_feed.usecase.GetFeedActivitiesUseCase
import com.gf.apkcarrera.features.f5_profile.usecase.GetProfileUseCase
import com.gf.apkcarrera.features.f5_profile.usecase.UpdateProfileUseCase
import com.gf.common.entity.user.UserModel
import com.gf.common.platform.BaseViewModel
import com.gf.common.response.GenericResponse
import com.gf.common.response.ProfileResponse
import com.gf.common.response.ProfileUpdateResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val getProfileUseCase: GetProfileUseCase,
    val updateProfileUseCase: UpdateProfileUseCase
): BaseViewModel() {

    // 1. Profile
    private val _profileState = MutableStateFlow<ProfileResponse?>(null)
    val profileState = _profileState.asStateFlow()

   // 2. Update profile
    private val _profileUpdatedState = MutableStateFlow<ProfileUpdateResponse?>(null)
    val profileUpdatedState = _profileUpdatedState.asStateFlow()

    fun getProfile(userId : String?) = launch {
        _profileState.emit(getProfileUseCase(userId))
    }

    fun updateProfile(userId: String, name : String, image : String) = launch {
        _profileUpdatedState.emit(updateProfileUseCase(userId,name,image))
    }

    fun profileUpdated(newProfile : UserModel) = launch {
        _profileState.emit(newProfile)
    }


}