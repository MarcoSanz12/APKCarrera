package com.gf.common.response

sealed class FriendResponse {
    class Succesful(
        val friendId : String
    ) : FriendResponse()
    object Error : FriendResponse()
}