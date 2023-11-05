package com.gf.common.entity.user

import android.graphics.Bitmap
import com.gf.common.entity.Resource

data class ImageResouce(
    val name: String,
    val bitmap: Bitmap
) : Resource() {
    override fun getResourceType(): ResourceType = ResourceType.IMAGE

    override fun getID(): Int = 0

    override fun getName(): String = name

}