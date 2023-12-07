package com.gf.common.response

import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.user.UserModel

sealed class ProfileResponse {
    class Succesful(val user : UserModel,val activityList : List<ActivityModel>) : ProfileResponse()

    object Error : ProfileResponse()
}