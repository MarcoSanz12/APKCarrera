package com.gf.common.entity

import java.io.Serializable

abstract class Resource : Serializable {

    abstract fun getResourceType(): ResourceType
    abstract fun getID(): Int
    abstract fun getName(): String

    enum class ResourceType(var char: String) {
        IMAGE("image")
    }


}