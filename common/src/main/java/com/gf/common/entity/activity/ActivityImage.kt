package com.gf.common.entity.activity

import android.graphics.Bitmap
import com.gf.common.entity.Resource

data class ActivityImage(
    val id : Int,
    val image : Bitmap

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