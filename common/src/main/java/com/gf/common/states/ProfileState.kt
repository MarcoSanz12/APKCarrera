package com.gf.common.states

import com.gf.common.entity.user.UserModel

sealed class ProfileState {
    data class Successful(
        val user : UserModel,
        val isFriend : Boolean,

    ) : ProfileState()

    object Error : ProfileState()
}