package com.gf.common.entity

import com.gf.common.entity.friend.FriendModel
import com.gf.common.entity.friend.FriendStatus
import com.gf.common.entity.user.UserModel

fun UserModel.toFriendModel() = FriendModel(
    uid = this.uid,
    uname = this.name,
    image = this.picture
)

fun UserModel.toFriendModel(user : UserModel) = FriendModel(
    uid = this.uid,
    uname = this.name,
    image = this.picture,
    friendStatus = assignFriendStatus(friend = this, user = user)



)

private fun assignFriendStatus(friend : UserModel, user:UserModel): FriendStatus {

    // 1. Le tengo agregado
    val addedByMe = user.friendList.contains(friend.uid)

    // 2. Me tiene agregado
    val hasMeAdded = friend.friendList.contains(user.uid)

    // 1. Los 2 se tienen agregados, son AMIGOS
    return if (addedByMe && hasMeAdded)
        FriendStatus.FRIEND
    // 2. Le tengo agregado, esperando a que ME ACEPTE
    else if (addedByMe)
        FriendStatus.ADDED_BY_ME
    // 3. Me tiene agregado, esperando a que LE ACEPTE
    else if (hasMeAdded)
        FriendStatus.ADDED_ME
    // 4. No nos conocemos
    else
        FriendStatus.UNKNOWN
}


