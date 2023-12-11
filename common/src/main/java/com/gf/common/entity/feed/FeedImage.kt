package com.gf.common.entity.feed

import com.gf.common.entity.Resource
import com.gf.common.entity.activity.ActivityImage

data class FeedImage (
    val id : Int,
    val url : String

) : Resource() {
    override fun getResourceType(): ResourceType = ResourceType.IMAGE

    override fun getID(): Int = id

    override fun getName(): String = "Image $id"

    override fun equals(other: Any?): Boolean {

        return if (other !is ActivityImage)
            false
        else
            getID() == other.getID()
    }
}