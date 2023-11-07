package com.gf.common.functional

import com.gf.common.entity.Resource

class FilterCategory(
    val title : String,
    var isChecked : Boolean = true
) : Resource(){
     constructor(filterCategory: FilterCategory) : this(
         filterCategory.title,
         filterCategory.isChecked)
    fun toggle() : Boolean {
        isChecked = !isChecked
        return isChecked
    }

    override fun getResourceType(): ResourceType = ResourceType.FILTRO

    override fun getID(): Int = 0

    override fun getName(): String = title

    override fun equals(other: Any?): Boolean {
        return if (other !is FilterCategory)
           false
        else
            other.getName() == this.getName()
    }
}


