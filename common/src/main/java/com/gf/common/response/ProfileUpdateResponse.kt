package com.gf.common.response

import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel

sealed class ProfileUpdateResponse {
    class Succesful(val updatename : String, val updatepicture : String) : ProfileUpdateResponse()

    data object Error : ProfileUpdateResponse()
}