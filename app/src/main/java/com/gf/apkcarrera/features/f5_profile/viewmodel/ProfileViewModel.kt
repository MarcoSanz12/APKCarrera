package com.gf.apkcarrera.features.f5_profile.viewmodel

import com.gf.apkcarrera.features.f5_profile.usecase.GetProfileUseCase
import com.gf.common.platform.BaseViewModel
import com.gf.common.response.ProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val getProfileUseCase: GetProfileUseCase
): BaseViewModel() {

    // 1. Profile
    private val _profileState = MutableStateFlow<ProfileResponse?>(null)
    val profileState = _profileState.asStateFlow()

    fun getProfile(userId : String?) = launch {
        _profileState.emit(getProfileUseCase(userId))
    }
}