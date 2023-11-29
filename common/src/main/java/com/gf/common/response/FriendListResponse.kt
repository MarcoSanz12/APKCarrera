package com.gf.common.response

import com.gf.common.entity.friend.FriendModel

sealed class FriendListResponse {
    class Succesful(
        val friendList : List<FriendModel>
    ) : FriendListResponse()

    object Error : FriendListResponse()
}