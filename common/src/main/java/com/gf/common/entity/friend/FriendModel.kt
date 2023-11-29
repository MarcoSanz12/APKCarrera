package com.gf.common.entity.friend

import com.gf.common.entity.Resource
import kotlin.random.Random

data class FriendModel(
    val uid: String = "",
    val uname : String = "",
    val image : String = "",
    var friendStatus : FriendStatus = FriendStatus.UNKNOWN
) : Resource() {

    val randomNumber = Random(7777L).nextInt()
    override fun getResourceType(): ResourceType = ResourceType.FRIEND
    override fun getID(): Int = randomNumber
    override fun getName(): String = uname
}

enum class FriendStatus{
    FRIEND,
    ADDED_BY_ME,
    ADDED_ME,
    UNKNOWN
}